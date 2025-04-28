class Train {
    private int id;
    private String serviceType;
    private String serviceCompany;

    private String departure;
    private String destination;
    private String departureName;
    private String destinationName;

    /**
     * Platform at the current station (departure)
     */
    private String platform;
    /**
     * Platform at the "destination" station
     */
    private String platformDestination;

    /**
     * When the train will leave the "departure" station
     */
    private long timeDeparture;

    /**
     * When the train will arrive at its "destination" station
     */
    private long timeDestinationArrival;

    public Train(int id, String serviceType, String serviceCompany, String departure, String destination,
            String departureName, String destinationName,
            String platform, String platformDestination, long timeDeparture, long timeDestinationArrival) {
        this.id = id;
        this.serviceType = serviceType;
        this.serviceCompany = serviceCompany;

        this.departure = departure;
        this.destination = destination;
        this.departureName = departureName;
        this.destinationName = destinationName;
        this.platform = platform;
        this.platformDestination = platformDestination;

        this.timeDeparture = timeDeparture;
        this.timeDestinationArrival = timeDestinationArrival;
    }

    public int getID() {
        return id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getDepartureStation() {
        return departure;
    }

    public String getArrivalStation() {
        return destination;
    }

    public String getDepartureStationName() {
        return departureName;
    }

    public String getArrivalStationName() {
        return destinationName;
    }

    public String getDeparturePlatform() {
        return platform;
    }

    public String getArrivalPlatform() {
        return platformDestination;
    }

    public long getDepartureTime() {
        return timeDeparture;
    }

    public long getArrivalTime() {
        return timeDestinationArrival;
    }

    public long getTripLength() {
        return timeDestinationArrival - timeDeparture;
    }

    public long timeToDeparture(long currentTime) {
        if (currentTime > timeDeparture) {
            System.out
                    .println("Warning! Current time is after departure time for train " + id + "from " + routeString());
        }
        return timeDeparture - currentTime;
    }

    // Data transfer between trains. Used for printing the Route
    public void setDestinationData(Train train) {
        this.destination = train.destination;
        this.destinationName = train.destinationName;
        this.platformDestination = train.platformDestination;
        this.timeDestinationArrival = train.timeDestinationArrival;
    }

    private String routeString() {
        String platformString = platform.length() == 0 ? "?" : platform;
        String departureStation = departureName + " (" + platformString + ")";
        String platformDestinationString = platformDestination.length() == 0 ? "?" : platformDestination;
        String destinationStation = destinationName + " (" + platformDestinationString + ")";

        return departureStation + " --> " + destinationStation;
    }

    private String formatTime(long time) {
        return String.format("%02d:%02d", (time / 3600) % 24, (time / 60) % 60);
    }

    public Train copy() {
        return new Train(id, serviceType, serviceCompany, departure, destination,
                departureName, destinationName,
                platform, platformDestination, timeDeparture, timeDestinationArrival);
    }

    @Override
    public String toString() {
        String start = id + " (" + serviceCompany + "): ";
        StringBuilder sb = new StringBuilder();

        // Unfortunately this code is duplicated here for the .repeat
        String platformString = platform.length() == 0 ? "?" : platform;
        String departureStation = departureName + " (" + platformString + ")";

        sb.append(start + routeString());
        sb.append("\n" + " ".repeat(start.length()) + formatTime(timeDeparture)
                + " ".repeat(departureStation.length() - String.valueOf(timeDeparture).length()) + " --> "
                + formatTime(timeDestinationArrival));

        return sb.toString();
    }
}
