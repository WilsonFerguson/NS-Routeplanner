import java.util.*;

class Route {
    LinkedList<Train> path;
    long duration;

    int numTrips;

    Station departureStation;
    Station arrivalStation;

    public Route(int numTrips) {
        this.numTrips = numTrips;

        path = new LinkedList<>();
        departureStation = null;
        arrivalStation = null;
    }

    public int getTrips() {
        return numTrips;
    }

    public void increment(long time) {
        duration += time;
    }

    public void decrement(long time) {
        duration -= time;
    }

    public void add(Train train, Station departureStation, Station arrivalStation) {
        path.add(train);

        if (this.departureStation == null) {
            this.departureStation = departureStation;
        }
        this.arrivalStation = arrivalStation;
    }

    public void removeLast(Station departureStation) {
        if (path.size() == 0) {
            System.out.println("Tried to remove train from route's path but size is 0!");
            return;
        }
        path.removeLast();

        if (path.size() == 0) {
            departureStation = null;
            arrivalStation = null;
        } else {
            arrivalStation = departureStation;
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
    }

    public void setArrivalStation(Station arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public Route copy() {
        Route route = new Route(numTrips);
        route.duration = duration;
        route.path = new LinkedList<>(path);

        return route;
    }

    private class Subroute {
        LinkedList<String> stations;
        long duration;
        long startTime;
        String serviceType;

        String startPlatform;
        String endPlatform;

        public Subroute(String serviceType, long startTime, String startPlatform) {
            stations = new LinkedList<>();
            duration = 0;
            this.startTime = startTime;
            this.serviceType = serviceType;

            this.startPlatform = startPlatform;
        }

        public void add(String station, long duration) {
            stations.add(station);
            this.duration += duration;
        }

        public void setEndPlatform(String endPlatform) {
            this.endPlatform = endPlatform;
        }
    }

    private String formatTime(long time) {
        return ((time / 3600) % 24) + String.format(":%02d", (time / 60) % 60);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        LinkedList<Subroute> subroutes = new LinkedList<>();

        sb.append("\n" + departureStation.getName() + " --> " + arrivalStation.getName());
        if (numTrips > 1) {
            sb.append("\n  " + formatTime(getDuration()) + " | (" + (numTrips - 1) + " transfer"
                    + (numTrips - 1 > 1 ? "s" : "") + ")\n\n");
        } else {
            sb.append("\n\n");
        }

        Subroute subroute = null;
        int id = -1;
        String endPlatform = null;
        for (int i = 0; i < path.size(); i++) {
            Train t = path.get(i);
            if (subroute == null) {
                subroute = new Subroute(t.getServiceType(), t.getDepartureTime(), t.getDeparturePlatform());
                subroute.add(t.getDepartureStationName(), (long) 0);
                subroute.add(t.getArrivalStationName(), t.getTripLength());
                endPlatform = t.getArrivalPlatform();

                id = t.getID();
                continue;
            }

            if (id == t.getID()) {
                long stationWaitingTime = t.getDepartureTime() - path.get(i - 1).getArrivalTime();
                long deltaDuration = t.getTripLength() + stationWaitingTime;

                subroute.add(t.getArrivalStationName(), deltaDuration);
                endPlatform = t.getArrivalPlatform();
            } else {
                subroute.setEndPlatform(endPlatform);
                subroutes.add(subroute);

                subroute = new Subroute(t.getServiceType(), t.getDepartureTime(), t.getDeparturePlatform());
                subroute.add(t.getDepartureStationName(), 0);
                subroute.add(t.getArrivalStationName(), t.getTripLength());

                id = t.getID();
            }
        }
        if (subroute != null) {
            subroute.setEndPlatform(endPlatform);
            subroutes.add(subroute);
        }

        int paddingWidth = 0;
        for (int i = 0; i < subroutes.size(); i++) {
            Subroute sr = subroutes.get(i);
            String timeDuration = formatTime(sr.duration);
            String service = sr.serviceType;
            paddingWidth = Math.max(paddingWidth, Math.max(service.length(), timeDuration.length()));
        }

        for (int i = 0; i < subroutes.size(); i++) {
            Subroute sr = subroutes.get(i);

            String timeDuration = formatTime(sr.duration);
            timeDuration = " ".repeat(paddingWidth - timeDuration.length()) + timeDuration;

            String service = sr.serviceType;
            service = " ".repeat(paddingWidth - service.length()) + service;

            String departureStation = sr.stations.get(0)
                    + ((sr.startPlatform != null && sr.startPlatform.length() > 0) ? " (" + sr.startPlatform + ")"
                            : "");
            if (i > 0)
                sb.append("\n");
            sb.append(" ".repeat(paddingWidth) + departureStation + " (" + formatTime(sr.startTime) + ")");

            if (sr.stations.size() == 2) {
                sb.append("\n" + timeDuration + "|");
                sb.append("\n" + service + "|");
            } else {
                sb.append("\n" + " ".repeat(paddingWidth) + "|");

                int timeIndex = (sr.stations.size() - 1) / 2;
                int serviceIndex = timeIndex + 1;
                for (int j = 1; j < sr.stations.size() - 1; j++) {
                    String station = sr.stations.get(j);

                    String start = " ".repeat(paddingWidth);
                    if (j == timeIndex) {
                        start = timeDuration;
                    } else if (j == serviceIndex) {
                        start = service;
                    }
                    sb.append("\n" + start + "| " + station);
                }

                if (sr.stations.size() - 2 < serviceIndex) {
                    sb.append("\n" + service + "|");
                    sb.append("\n" + " ".repeat(paddingWidth) + "|");
                } else {
                    sb.append("\n" + " ".repeat(paddingWidth) + "|");
                }
            }

            String arrivalStation = sr.stations.get(sr.stations.size() - 1)
                    + ((sr.endPlatform != null && sr.endPlatform.length() > 0) ? " (" + sr.endPlatform + ")" : "");
            sb.append("\n" + " ".repeat(paddingWidth) + arrivalStation + " (" + formatTime(sr.startTime + sr.duration)
                    + ")");

            if (i < subroutes.size() - 1) {
                long transferTime = subroutes.get(i + 1).startTime - sr.startTime - sr.duration;
                String transferTimeString = null;
                if (transferTime < 60) {
                    transferTimeString = transferTime + " s";
                } else if (transferTime < 3600) {
                    transferTimeString = (transferTime / 60) + " m";
                } else {
                    transferTimeString = (transferTime / 3600) + " h";
                }
                sb.append("\n");
                sb.append("\n" + " ".repeat(paddingWidth) + "(" + transferTimeString + " wait)");
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
