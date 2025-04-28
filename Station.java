import java.util.*;

import javax.security.auth.PrivateCredentialPermission;

class Station {
    private String id;
    private String name;
    private ArrayList<Train> departures;

    public Station(String id) {
        this.id = id;
        departures = new ArrayList<>();
    }

    public ArrayList<Train> getDepartures() {
        return departures;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public int getDepartureCount() {
        return departures.size();
    }

    public List<Train> getDepartures(long startTime, long endTime) {
        // I'm fine to waste a bit of memory to increase speed
        List<Train> validDepartures = new ArrayList<>(departures.size());

        for (Train train : departures) {
            long departureTime = train.getDepartureTime();
            if (departureTime >= startTime && departureTime <= endTime)
                validDepartures.add(train);
        }

        return validDepartures;
    }

    private static String formatTime(long time) {
        return ((time / 3600) % 24) + String.format(":%02d", (time / 60) % 60);
    }

    public List<Train> getDepartures(String platform, int trainID, long startTime, long minAbsoluteTime,
            long minTransferTime,
            long maxTransferTime) {
        List<Train> validDepartures = new ArrayList<>(departures.size());

        for (Train train : departures) {
            long departureTime = train.getDepartureTime();
            // If it's the same train as currently on, then add it
            if (train.getID() == trainID) {
                validDepartures.add(train);
                continue;
            }

            // Discard any trains that come before this
            if (departureTime < startTime + minAbsoluteTime) {
                continue;
            }

            // If we are the first station, then we can add any train
            if (platform == null) {
                validDepartures.add(train);
                continue;
            }

            // If the train is in the time range, add it
            if (departureTime >= startTime + minTransferTime && departureTime <= startTime + maxTransferTime) {
                validDepartures.add(train);
                continue;
            }

            // If it's out of the time range but the platforms differ by <= 1, add it
            // (cross-platform transfer idea)
            String platformCurrent = platform.replaceAll("[^0-9]", ""); // ^ negates when in breackets
            String platformFuture = train.getDeparturePlatform().replaceAll("[^0-9]", "");
            try {
                int diff = Integer.parseInt(platformFuture) - Integer.parseInt(platformCurrent);
                if (Math.abs(diff) <= 1) {
                    validDepartures.add(train);
                    continue;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return validDepartures;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + " " + "(" + id + "):");
        for (Train t : departures) {
            sb.append("\n" + t);
        }
        sb.append("\n");
        return sb.toString();
    }
}
