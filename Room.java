package com.company;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;


class Room {

    private String name;
    private String location;
    private int capacity;
    private ArrayList<Booking> bookings;
    private static int largestRoomCapacity;

    Room(String name, String location, int capacity) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.bookings = new ArrayList<>();

        if(capacity > largestRoomCapacity){
            largestRoomCapacity = capacity;
        }
    }

    String getName() {
        return name;
    }

    String getLocation() {
        return location;
    }

    int getCapacity() {
        return capacity;
    }


    public boolean checkRoomCapacity(Room room, int capacity){
        return room.getCapacity() >= capacity;                          //Returns true if a room can hold the required capacity
    }

    static int getLargestRoomCapacity() {
        return largestRoomCapacity;
    }

    ArrayList<Booking> getBookings() {
        return bookings;
    }

    void makeBooking(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, int capacity, String booker){
        Booking newBooking = new Booking(dateTimeStart, dateTimeEnd, capacity, booker, this.name);
        sortBooking(newBooking);
        writeBookingToFile(newBooking.toCSV());
    }

    void makeBooking(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, int capacity, String booker, int id){
        //Overloaded Constructor so that the correct ID's can be given to the bookings when the session ends and is read back on session restart
        Booking newBooking = new Booking(dateTimeStart, dateTimeEnd, capacity, booker, this.name, id);
        sortBooking(newBooking);
    }

    private void sortBooking(Booking newBooking){
        //Bookings are sorted by date and time for the check constraints function to check the booking before and after to ensure no collisions occur when checking bookings
        if(bookings.size() > 0) {
            for (int i = 0; i < bookings.size(); i++) {
                Booking checkBooking = bookings.get(i);
                if (newBooking.getDateTimeStart().isBefore(checkBooking.getDateTimeStart())) {
                    this.bookings.add(i, newBooking);
                    return;
                }
            }
        }
        this.bookings.add(newBooking);
    }

    private Booking findBooking(int id){

        for(Booking booking : this.bookings){
            if(id == booking.getId()){
                return booking;
            }
        }
        return null;
    }

    void deleteBooking(int id){
        Booking booking = findBooking(id);
        if(booking != null){
            System.out.println("Booking for " + booking.getBooker() + " at " + booking.getRoomName() + " for " + booking.getCapacityRequired() + " people and with ID of " + booking.getId() + " has been deleted");
            bookings.remove(findBooking(id));
            deleteBookingFromFile(id);
        }
        else{
            System.out.println("Couldn't find booking");
        }
    }

    private void writeBookingToFile(String toWrite){
        FileWriter writer;
        PrintWriter print;
        try{
            writer = new FileWriter("bookings.txt",true);
            print = new PrintWriter(writer);
            print.append(toWrite).append("\n");

            print.close();
            writer.close();
        }catch(Exception e){
            //Catch Error
        }
    }

    private void deleteBookingFromFile(int id){
        Scanner s = null;
        FileWriter writer;
        PrintWriter print;
        boolean fileFound = false;
        try{
            s = new Scanner(new BufferedReader(new FileReader("bookings.txt")));
            while(s.hasNext()){
                    fileFound = true;
                    String fileString = s.nextLine();
                    String[] sArray = fileString.split(",");
                    int tempId = Integer.parseInt(sArray[0]);
                    if(tempId != id){
                        try{
                            writer = new FileWriter("temp.txt",true);
                            print = new PrintWriter(writer);
                            print.write(fileString+"\n");
                            print.close();
                            writer.close();
                        }catch(Exception e){
                            //Catch error
                        }
                    }
            }
            s.close();

            File bookings = new File("bookings.txt");
            File temp = new File("temp.txt");
            boolean b = bookings.delete();
            boolean t = temp.renameTo(bookings);
            if(!b)
                System.out.println("Unable to delete rooms file");
            if(temp.exists()) {
                if (!t)
                    System.out.println("Unable to rename temp file");
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Default rooms have been added");
        }finally{
            if(fileFound) {
                s.close();
            }
        }
    }
}
