package com.company;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

public class Main {
    private static Department nsa = new Department();
    private static Scanner scanner = new Scanner(System.in);

    //The below variables are for formatting the bookings read from "history.txt"
    //THESE MAY NOT SHOW UP ON WINDOWS CMD. INTELLIJ IS ADVISED FOR FULL EXPERIENCE
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {
        appSetup(); //Reads records from previous sessions.

        int choice;
        boolean adminMode=false;
        while(true){
            displayMenu(adminMode);
            System.out.println("Please enter your choice: ");
            try {
                choice = scanner.nextInt();

            }catch(Exception e){
                //Stops program failing abruptly if user enters a non-numerical input. Set to -1 for default switch case to catch the error.
                choice = -1;
            }
            scanner.nextLine();

            if(!adminMode) {
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        viewAllRooms();
                        break;
                    case 2:
                        makeBooking();
                        break;
                    case 3:
                        printMyBookings();
                        break;
                    case 4:
                        deleteBooking(false);
                        break;
                    case 100:
                        adminMode = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Input must be chosen via the index of the option (number)");
                        break;
                }
            }
            else{
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        viewAllRooms();
                        break;
                    case 2:
                        makeBooking();
                        break;
                    case 3:
                        printMyBookings();
                        break;
                    case 4:
                        deleteBooking(true);    //Admins can delete any booking
                        break;
                    case 5:
                        addRoom();
                        break;
                    case 6:
                        printAllUpcomingBookings();
                        break;
                    case 100:
                        adminMode = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Input must be chosen via the index of the option (number)");
                        break;
                }
            }
        }
    }

    private static void printAllUpcomingBookings() {
        ArrayList<Booking> bookings = nsa.getAllUpcomingBookings();
        for(Booking booking: bookings){
            System.out.print("ID: " + booking.getId() + " Start Time: " + booking.getDateTimeStart().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) +
                    " End Time: " + booking.getDateTimeEnd().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) + " Capacity: " + booking.getCapacityRequired() + " Room: " +
                    booking.getRoomName() + "\n");
        }
    }

    private static void appSetup(){
        nsa.readRoomsFromFile();
        nsa.moveBookingsToHistory();
        nsa.readBookingsFromFile();
    }

    private static void displayMenu(boolean adminMode){
        if(!adminMode) {
            System.out.println("\nMain Menu (Enter 100 for admin mode)");
            System.out.println("0. Exit");
            System.out.println("1. View All Rooms");
            System.out.println("2. Make Booking");
            System.out.println("3. View Bookings");
            System.out.println("4. Delete Booking");
        }
        else{
            System.out.println("\nMain Menu (Enter 100 for user mode)");
            System.out.println("0. Exit");
            System.out.println("1. View All Rooms");
            System.out.println("2. Make Booking");
            System.out.println("3. View Bookings");
            System.out.println("4. Delete Booking");
            System.out.println("5. Add Room");
            System.out.println("6. View All Bookings");
        }
    }

    private static void viewAllRooms(){
        nsa.listAllRooms();
    }

    private static void printMyBookings(){
        boolean history=false, choice=false;
        String name = getName();
        while(!choice) {
            System.out.print("Would you like to view all bookings? press A for all, U for upcoming bookings: ");
            char c = scanner.next().charAt(0);
            if(c == 'U' || c == 'u'){
                choice = true;
            }
            else if(c == 'A' || c== 'a'){
                history = true;
                choice = true;
            }
            else{
                System.out.println("Input must be A for all bookings, or U for upcoming only");
                choice = false;
            }

        }
        ArrayList<Booking> myBookings = nsa.getMyBookings(name, history);
        if(myBookings == null){
            System.out.println("No bookings have been made by: " + name);
            return;
        }

        if(history) {
            System.out.println("All bookings made by: " + name.toUpperCase());
            for (Booking booking : myBookings) {
                if(booking.getDateTimeEnd().isBefore(LocalDateTime.now())){
                    System.out.print( ANSI_RED + "EXPIRED: ");
                }
                else{
                    System.out.print("ID: " + booking.getId() + " ");
                }
                System.out.print("Start Time: " + booking.getDateTimeStart().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) +
                        " End Time: " + booking.getDateTimeEnd().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) + " Capacity: " + booking.getCapacityRequired() + " Room: " +
                        booking.getRoomName() + "\n" + ANSI_RESET);
            }
        }
        else {
            printUpcomingBookings(name, myBookings);
        }
    }

    private static void printUpcomingBookings(String name, ArrayList<Booking> myBookings) {
        System.out.println("Upcoming bookings made by: " + name.toUpperCase());
        for (Booking booking : myBookings) {
            System.out.println("ID: " + booking.getId() + " Start Time: " + booking.getDateTimeStart().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) +
                    " End Time: " + booking.getDateTimeEnd().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) + " Capacity: " + booking.getCapacityRequired() + " Room: " +
                    booking.getRoomName());
        }
    }

    private static void makeBooking(){
        ArrayList<Room> rooms;
        int capacity=0;
        boolean confirmed=false;
        LocalDate date=null;
        LocalTime time=null, period;
        LocalDateTime bookingEnd = null;
        String booker="";

        while(!confirmed) {
            //Gets all user constraints
            capacity = getCapacity();
            date = getDate();
            time = getTime(date);
            period = getPeriod(date, time);
            bookingEnd = getEndTime(date, time, period);
            //Allows the user to double check their inputs
            System.out.println("\nYour requirements are:\n" +
                    "Capacity: " + capacity + "\n" +
                    "Start Date: " + date.getDayOfWeek() + " the " + date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")) + "\n" +
                    "Start Time: " + time.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
                    "End Date: " + bookingEnd.getDayOfWeek() + " the " + bookingEnd.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")) + "\n" +
                    "End Time: " + bookingEnd.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n");
            confirmed = getConfirmation();
        }
        //Parsing user Date and time constraints
        LocalDateTime startTime = date.atStartOfDay().plusHours(time.getHour()).plusMinutes(time.getMinute());
        LocalDateTime endTime = bookingEnd;

        rooms = nsa.getAvailableRooms(startTime,endTime,capacity);

        if(rooms == null){
            return;
        }
        confirmed = false;
        while(!confirmed) {
            booker = getName();
            System.out.println("Your username is: " + booker);
            confirmed = getConfirmation();
        }

        for(int i=0;i<rooms.size();i++){
            Room room = rooms.get(i);
            System.out.println(i+1 + ". " + room.getName() + " - " + room.getCapacity() + " - " + room.getLocation());
        }
        getRoomNoAndBook(startTime, endTime, capacity, booker, rooms);
    }

    private static void deleteBooking(boolean adminMode){
        int id;
        String name;
        ArrayList<Booking> myBookings;

        if (!adminMode) {
            name = getName();
            //History included is hardcoded as the user has no ability to delete historical bookings
            myBookings = nsa.getMyBookings(name,false);
            if(myBookings == null){
                System.out.println("No bookings exist by that name");
                return;
            }
            printUpcomingBookings(name, myBookings);
        }
        else{
            myBookings = nsa.getAllUpcomingBookings();
            if(myBookings == null){
                System.out.println("No bookings exist by that name");
                return;
            }
            printAllUpcomingBookings();
        }

        System.out.println("Please enter the ID of the booking you wish to delete, or 0 to quit");
        try {
            id = scanner.nextInt();
        }catch(InputMismatchException e){
            System.out.println("ID must be a number");
            scanner.nextLine();
            return;
        }

        scanner.nextLine();
        if(id == 0){
            return;
        }
        for(Booking booking : myBookings){
            if(booking.getId() == id){
                nsa.deleteBooking(id);
                return;
            }
        }
        System.out.println("No bookings with that ID and Name exist");
    }

    private static int getCapacity(){
        boolean checked = false, isInt=true;
        int capacity=0;

        while(!checked) {
            System.out.print("Please input your capacity(Number): ");
            try {
                capacity = scanner.nextInt();

            }catch(InputMismatchException e){
                System.out.println("Capacity must be a number");
                isInt = false;
            }
            scanner.nextLine();

            if (capacity < 1 && isInt) {
                System.out.println("Capacity must be greater than or equal to 1");
            }
            else if(Room.getLargestRoomCapacity() < capacity && isInt){
                System.out.println("Our max capacity is " + Room.getLargestRoomCapacity());     //Lets the user know if the number of attendees is greater than largest room
            }
            else if(isInt){
                checked = true;
            }
            isInt = true;
        }
        return capacity;
    }

    private static LocalDate getDate(){
        boolean checked = false;
        String date;
        LocalDate newDate = null;

        while(!checked){
            System.out.print("Please input your booking date(DD/MM/YYYY): ");
            date = scanner.nextLine();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);  //Used for British date convention
            try {
                newDate = LocalDate.parse(date, formatter);   //Saves program from crashing if user inputs wrong format
                if(newDate.isBefore(LocalDate.now())){
                    System.out.println("Invalid date. Can't make bookings in the past");
                    checked = false;
                }
                else if(newDate.getDayOfWeek() == DayOfWeek.SATURDAY || newDate.getDayOfWeek() == DayOfWeek.SUNDAY){    //Ensures bookings can't be made on weekends
                    System.out.println("The department is closed on weekends. Opening hours are 08:30 to 16:30, Monday - Friday");
                    checked = false;
                }
                else if(newDate.isEqual(LocalDate.now()) && (LocalTime.now().isAfter(LocalTime.of(16,30)))){    //Ensures bookings can't be made after 16:30 (closing time)
                    System.out.println("The department is closed for today. Opening hours are 08:30 to 16:30");               //on same day
                    checked = false;
                }
                else{
                    checked = true;
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Invalid date. Format must be in same format as examples: 'DD/MM/YYYY'");
            }
        }
        return newDate;
    }

    private static LocalTime getTime(LocalDate date){
        boolean checked = false;
        String time;
        LocalTime newTime = null;
        LocalDateTime newDateTime = date.atStartOfDay();

        while(!checked){
            System.out.print("Please input your start time(HH:MM): ");
            time = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);  //Used for British date convention
            try {
                newTime = LocalTime.parse(time, formatter);   //Saves program from crashing if user inputs wrong format
                newDateTime = newDateTime.plusHours(newTime.getHour()).plusMinutes(newTime.getMinute());
                //Limit is set to 16:15 as any time after that would be illogical with the closing time at 16:30
                if(newTime.isBefore(LocalTime.parse("08:30:00")) || newTime.isAfter(LocalTime.parse("16:15:00"))){
                    System.out.println("Open hours are 08:30AM to 4:30PM");
                    checked = false;
                }
                else if(newDateTime.isBefore(LocalDateTime.now()) ){
                    System.out.println("Invalid time. Can't make bookings in the past");
                    checked = false;
                }
                else{
                    checked = true;
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Invalid time. Format must be in same format as examples: 'HH:MM'");
            }
        }
        return newTime;
    }

    private static LocalTime getPeriod(LocalDate date, LocalTime time){
        //(t)ime is passed in to ensure that the period does not overrun the opening hours of 08:30 to 16:30
        boolean checked = false;
        String inputPeriod;
        LocalTime newPeriod = null;

        while(!checked){
            System.out.print("Please input your period(HH:MM): ");
            inputPeriod = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);  //Used for British date convention
            try {
                newPeriod = LocalTime.parse(inputPeriod, formatter);
                //Checks that the end time is not after closing time, checks that the period is no longer than the max period of 8 hours (e.g, opening time to closing time),
                //And checks that the maximum period cannot cycle in to the forthcoming day. e.g. a booking at 4pm for 8 hours ends at 1:00am
                LocalDateTime endDateTime = getEndTime(date, time, newPeriod);
                LocalDate endDate = endDateTime.toLocalDate();
                LocalTime endTime = endDateTime.toLocalTime();
                if(endTime.minusMinutes(1).isAfter(LocalTime.parse("16:30")) || endDate.isAfter(date)){
                    System.out.println("Closing time is at 4:30pm");
                    checked=false;
                }
                else {
                    checked = true;
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Invalid period. Format must be in same format as examples: 'HH:MM'");
            }
        }
        return newPeriod;
    }

    private static LocalDateTime getEndTime(LocalDate dateTime, LocalTime startTime, LocalTime period){
        //Converts separate LocalDate and LocalTime variables to a LocalDateTime type where checks can be done against both date and time in one variable
        //we minus 1 minute to make sure that on the dot bookings can be made. e.g. a booking from 9:00 onwards is ok, if a booking is made from 8:30-8:59
        return dateTime.atStartOfDay().plusHours(startTime.getHour()).plusMinutes(startTime.getMinute()).plusHours(period.getHour()).plusMinutes(period.getMinute()).minusMinutes(1);
    }

    private static String getName(){
        boolean checked = false;
        String booker="";

        while(!checked) {
            System.out.print("Please input your name: ");
            try {
                booker = scanner.nextLine().toUpperCase();     //Takes in upper case input to ensure there's no ambiguity in entry format
                if(booker.isBlank() || booker.isEmpty()){
                    System.out.println("Name cannot be Blank or Empty");
                    checked = false;
                }
                else{
                    checked = true;
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        return booker;
    }

    private static boolean getConfirmation(){

        System.out.print("Is this right(Y)es or (N)o: ");
        while(true) {
            char confirmation = scanner.next().charAt(0);
            if (confirmation == 'Y' || confirmation == 'y') {
                scanner.nextLine();
                return true;
            } else if (confirmation == 'N' || confirmation == 'n') {
                scanner.nextLine();
                return false;
            } else {
                scanner.nextLine();
                System.out.println("Invalid input. Must be (Y)es or (N)o");
            }
        }
    }

    private static void getRoomNoAndBook(LocalDateTime startTime, LocalDateTime endTime, int capacity, String booker, ArrayList<Room> rooms){
        boolean checked = false;
        int choice=0;
        while(!checked) {
            System.out.println("If you'd like to book one of the available rooms, input their number. To quit input 0");
            try {
                choice = scanner.nextInt();
            }catch(Exception e){
                //Save program from dying unexpectedly
            }
            scanner.nextLine();
            if (choice == 0) {
                return;
            }
            else if (choice > rooms.size() || choice < 0) {
                System.out.println("Invalid number");
                checked = false;
            } else {
                //1 is taken from the choice variable to keep the ordering of rooms logical as they are presented to the user
                rooms.get(choice - 1).makeBooking(startTime, endTime, capacity, booker);
                System.out.println("Your booking has been made in " + rooms.get(choice - 1).getName() + " on " + startTime.getDayOfWeek() + " the " +
                        startTime.getDayOfMonth() + "/" + startTime.getMonthValue() + "/" + startTime.getYear() + " at " + startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " for " + capacity + " people" +
                        " Booked by: " + booker);
                checked = true;
            }
        }
    }

    private static void addRoom(){
        System.out.print("Enter the name of the room: ");
        String name = scanner.nextLine();
        System.out.print("Enter the location of the room: ");
        String location = scanner.nextLine();
        System.out.print("Enter the capacity of the room: ");
        int capacity;
        try {
            capacity = scanner.nextInt();
            scanner.nextLine();
        }catch(Exception e){
            System.out.println("Capacity must be a numerical value");
            return;
        }
        if(capacity < 1){
            System.out.println("Capacity must be greater than or equal to 1");
            return;
        }

        System.out.println(name + ", " + location + ", " + capacity + " has been added");
        nsa.addRoom(name,location,capacity,true);
    }

}