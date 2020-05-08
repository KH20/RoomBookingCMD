package com.company;

import java.time.LocalDateTime;

class Booking {
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTimeEnd;
    private int capacityRequired;
    private String booker;
    private static int bookingRecord=0; //A total number of bookings is kept so that a Booking ID can be automatically incremented on each Booking.
    private final int id;
    private String roomName;

    Booking(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, int capacityRequired, String booker, String roomName) {
        this.dateTimeStart = dateTimeStart;
        this.dateTimeEnd = dateTimeEnd;
        this.capacityRequired = capacityRequired;
        this.booker = booker;
        bookingRecord++;
        if(bookingRecord == Integer.MAX_VALUE){
            bookingRecord = 0;          //Stops bookingRecord potentially overflowing and becoming a negative value.
        }
        this.id = bookingRecord;
        this.roomName = roomName;
    }

    Booking(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, int capacityRequired, String booker, String roomName, int id) {
        this.dateTimeStart = dateTimeStart;
        this.dateTimeEnd = dateTimeEnd;
        this.capacityRequired = capacityRequired;
        this.booker = booker;
        this.id = id;
        this.roomName = roomName;
    }

    LocalDateTime getDateTimeStart() {
        return dateTimeStart;
    }

    LocalDateTime getDateTimeEnd() {
        return dateTimeEnd;
    }

    int getCapacityRequired() {
        return capacityRequired;
    }

    String getBooker() {
        return booker;
    }

    String getRoomName() {
        return roomName;
    }

    int getId() {
        return id;
    }

    static void setBookingRecord(int bookingRecord) {
        Booking.bookingRecord = bookingRecord;
    }

    String toCSV() {
        return id + "," + roomName + "," + dateTimeStart + "," + dateTimeEnd + "," + capacityRequired + "," + booker;
    }
}



