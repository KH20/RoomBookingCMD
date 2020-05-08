package com.company;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;


 class Department {
    private ArrayList<Room> rooms;

    Department() {
        this.rooms = new ArrayList<>();
    }

    void addRoom(String name, String location, int capacity, boolean writeToFile){
        //writeToFile is passed in as true if the room is added by an admin.
        //writeToFile is set to false when reading from file to protect against a continuous loop of reading and writing
        Room newRoom = new Room(name, location, capacity);
        rooms.add(newRoom);
        if(writeToFile)
            writeRoomToFile(name + "," + location + "," + capacity);
    }

    void listAllRooms(){
        for(Room roomChecked : rooms){
            System.out.println("Room Name: " + roomChecked.getName());
            System.out.println("Capacity: " + roomChecked.getCapacity());
            System.out.println("Location: " + roomChecked.getLocation());
            System.out.println("=====================");
        }
    }

    ArrayList<Room> getAvailableRooms(LocalDateTime timeStart, LocalDateTime timeEnd, int capacity){

        ArrayList<Room> roomsAvailable = new ArrayList<>();
        boolean booked;

        for(Room room : rooms){
            ArrayList<Booking> bookings = room.getBookings();
            booked = false;

            if(!room.checkRoomCapacity(room, capacity)){
                continue;       //Disregards any rooms that can't please the capacity constraints
            }
            if(bookings.size() >= 2){
                //Check is made for 2 or more bookings as the for loop checks the slot before and after the proposed new booking
                for (int i = 0; i < bookings.size() - 1; i++) { //minus 1 from booking because we check the current booking, and the one after
                    Booking checkBookingA = bookings.get(i);
                    Booking checkBookingB = bookings.get(i + 1);
                    //And ensure we can't book over a booking or
                    //extend a booking into another time slot
                    booked= checkTime(timeStart, timeEnd, checkBookingA.getDateTimeStart(), checkBookingA.getDateTimeEnd()) ||      //Checks the booking start time and end time
                            checkTime(timeStart, timeEnd, checkBookingB.getDateTimeStart(), checkBookingB.getDateTimeEnd());
                }
            }
            else if(bookings.size() == 1){
                //Check bookings don't overlap, then add to available rooms
                Booking checkBookingA = bookings.get(0);
                booked = checkTime(timeStart, timeEnd, checkBookingA.getDateTimeStart(), checkBookingA.getDateTimeEnd());
            }
            //No bookings found, add to available rooms
            if(!booked){
                roomsAvailable.add(room);
            }
        }
        if(roomsAvailable.size() == 0){
            //As no rooms have been added to roomsAvailable, Lets user know that no rooms are available based on their requirements
            System.out.println("No rooms satisfy your conditions");
            return null;
        }
        return roomsAvailable;
    }

    private boolean checkTime(LocalDateTime startTimeA, LocalDateTime endTimeA, LocalDateTime startTimeB, LocalDateTime endTimeB){
        //Checks that the user booking is either after the end time of the checked booking, or the end time is before the start time of the checked booking
        return !startTimeB.isAfter(endTimeA) && !endTimeB.isBefore(startTimeA);
    }

    ArrayList<Booking> getMyBookings(String name, boolean historyIncluded){
        //returns either all, or upcoming bookings based on name passed in and a boolean dictating whether the history
        //is included or not.
        ArrayList<Booking> myBookings = new ArrayList<>();
        for(Room room : rooms){
            for(Booking booking : room.getBookings()){
                if(booking.getBooker().equals(name)){
                    myBookings.add(booking);
                }
            }
        }
        if(historyIncluded) {
            ArrayList<Booking> history = getHistory(name);
            if(history != null)
                myBookings.addAll(history);
        }
        if(myBookings.size() == 0){
            return null;
        }
        //Sorts bookings by date start making the bookings easier to read and follow
        myBookings.sort((o1, o2) -> {
            if(o1.getDateTimeStart().isEqual(o2.getDateTimeStart()))
                return 0;
            else if(o1.getDateTimeStart().isBefore(o2.getDateTimeStart()))
                return 1;
            else
                return -1;
        });
        return myBookings;
    }

    ArrayList<Booking> getAllUpcomingBookings(){
        ArrayList<Booking> allBookings = new ArrayList<>();
        for(Room room : rooms){
            if(room.getBookings() != null)
                allBookings.addAll(room.getBookings());
        }
        allBookings.sort((o1, o2) -> {
            if(o1.getDateTimeStart().isEqual(o2.getDateTimeStart()))
                return 0;
            else if(o1.getDateTimeStart().isBefore(o2.getDateTimeStart()))
                return 1;
            else
                return -1;
        });

        return allBookings;
    }

    void readBookingsFromFile(){     //Used on app startup to save bookings across multiple sessions
        Scanner s = null;
        int bookingCount = 0;
        boolean filesExist = false;
        ArrayList<String> bookingArray = new ArrayList<>();
        try{
            s = new Scanner(new BufferedReader(new FileReader("bookings.txt")));
            while(s.hasNext()){
                filesExist=true;

                if(s.hasNextLine()){
                    bookingArray.add(s.nextLine());

                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("A new bookings file will be created if a booking is made");
        }finally{
            if(filesExist)
                s.close();
        }
        if(filesExist) {
            for (Room room : rooms) {
                for (String booking : bookingArray) {
                    String[] sArray = booking.split(",");
                    if (room.getName().equals(sArray[1])) {
                        room.makeBooking(LocalDateTime.parse(sArray[2]),LocalDateTime.parse(sArray[3]),Integer.parseInt(sArray[4]),sArray[5],Integer.parseInt(sArray[0]));
                    }
                    if(Integer.parseInt(sArray[0]) > bookingCount){
                        bookingCount = Integer.parseInt(sArray[0]);     //reads the highest booking count on file and sets the booking record to it to stop ID resetting on session end
                    }
                }
            }
            Booking.setBookingRecord(bookingCount);
        }
    }

    void deleteBooking(int id){
        for(Room room : rooms){
            for(Booking booking : room.getBookings()){
                if(id == booking.getId()){
                    room.deleteBooking(id);
                    return;
                }
            }
        }
    }

    private ArrayList<Booking> getHistory(String user){
        Scanner s = null;
        ArrayList<Booking> history = new ArrayList<>();
        boolean historyFound=false;
        try{
            s = new Scanner(new BufferedReader(new FileReader("history.txt")));
            while(s.hasNext()){
                historyFound=true;
                String historyString = s.nextLine();
                String[] sArray = historyString.split(",");
                if(sArray[5].equals(user)){
                    //Check for all records where the names match. ID is set to -1 as ID no longer matters as it's a historical booking
                    history.add(new Booking(LocalDateTime.parse(sArray[2]), LocalDateTime.parse(sArray[3]), Integer.parseInt(sArray[4]), sArray[5], sArray[1], -1));
                }
            }
        }catch(Exception e){
            //Catch Error
        }finally{
            if(historyFound)
                s.close();
        }

        if(history.size() == 0){
            return null;
        }
        return history;
    }

    void readRoomsFromFile(){            //If no rooms file can be found, it loads the default rooms that were supplied in the assignment brief
        Scanner s = null;
        boolean roomsExist = false;
        try{
            s = new Scanner(new BufferedReader(new FileReader("rooms.txt")));
            while(s.hasNext()){
                roomsExist=true;

                String[] sArray = s.nextLine().split(",");
                addRoom(sArray[0],sArray[1],Integer.parseInt(sArray[2]),false);

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Default rooms have been added");
        }finally{
            if(roomsExist)
                s.close();
            else{
                //Incase rooms.txt file goes missing, the default rooms for the assignment are added
                addRoom("Taff","Small meeting room on 2nd floor",8,false);
                addRoom("Llangorse","Large meeting room on 2nd floor",24,false);
                addRoom("Pen Y Fan","Teaching space on 2nd floor",70,false);
                addRoom("Usk","Small meeting room on 3rd floor",8,false);
                addRoom("Bala","Large meeting room on 3rd floor",24,false);
                addRoom("Cadair Idris","Teaching space on 3rd floor",70,false);
                addRoom("Wye","Small meeting room on 4th floor",8,false);
                addRoom("Gower","Open meeting/break-out space on 4th floor",24,false);
                addRoom("Snowdon","Teaching space on 4th floor",70,false);
                for(Room room : rooms){
                    writeRoomToFile(room.getName() + "," + room.getLocation() + "," + room.getCapacity());
                }
            }
        }
    }

    private void writeRoomToFile(String toWrite){
        FileWriter writer;
        PrintWriter print;
        try{
            writer = new FileWriter("rooms.txt",true);
            print = new PrintWriter(writer);
            print.append(toWrite).append("\n");

            print.close();
            writer.close();
        }catch(Exception e){
            //Catch Error
        }

    }

    void moveBookingsToHistory() {
        //When the saved bookings are now in the past, they get moved from Bookings and placed in History.
        //This is done to save time from the bookings arraylist being filled with needless bookings and
        //potentially taking too long to execute each time a booking is made as it would have to move all
        //current records in the arraylist.
        Scanner s = null;
        FileWriter writer;
        PrintWriter print;
        boolean fileFound = false;
        try {
            s = new Scanner(new BufferedReader(new FileReader("bookings.txt")));
            while (s.hasNext()) {
                fileFound = true;
                String temp = s.nextLine();
                String[] sArray = temp.split(",");
                LocalDateTime endDateTime = LocalDateTime.parse(sArray[3]);
                if (endDateTime.isBefore(LocalDateTime.now()) || endDateTime.isEqual(LocalDateTime.now())) {
                    writer = new FileWriter("history.txt", true);
                    print = new PrintWriter(writer);
                    print.write(temp + "\n");
                    print.close();
                    writer.close();
                } else if (endDateTime.isAfter(LocalDateTime.now())) {
                    writer = new FileWriter("temp.txt", true);
                    print = new PrintWriter(writer);
                    print.write(temp + "\n");
                    print.close();
                    writer.close();
                }
            }
            s.close();
            File bookings = new File("bookings.txt");
            File temp = new File("temp.txt");
            boolean b = bookings.delete();
            boolean t = temp.renameTo(bookings);
            if(!b)
                System.out.println("Unable to delete bookings file");
            if(temp.exists()) {
                if (!t)
                    System.out.println("Unable to rename temp file");
            }
        } catch (Exception e) {
            //Catch Error
        } finally {
            if (fileFound) {
                s.close();
            }
        }
    }
}
