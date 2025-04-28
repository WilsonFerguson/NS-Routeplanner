import java.io.*;
import java.util.*;

class Planner {
    private static Scanner scanner;

    private static String startID;
    private static String endID;
    private static long startTime;

    private static ArrayList<Route> routes;

    /**
     * All transfers must be at least this long
     */
    private static int minAbsoluteTime = (int) (0.3 * 60);
    /**
     * Non cross-platform transfers must abide by this minimum time
     */
    private static int minWaitTime = (int) (3 * 60);
    private static int maxWaitTime = (int) (2 * 3600 + 0.5 * 60);

    private static ArrayList<List<String>> schedule;
    private static ArrayList<Train> trains;

    /**
     * Maps an id to a station
     */
    private static HashMap<String, Station> stations;
    /**
     * Maps a station name to its id
     */
    private static HashMap<String, String> stationIDs;

    private static void loadSchedule() {
        schedule = new ArrayList<>();

        try {
            // https://www.baeldung.com/java-csv-file-array
            FileReader file = new FileReader("dayFinalForRealThisTime.csv");
            BufferedReader br = new BufferedReader(file);

            String line;
            while ((line = br.readLine()) != null) {
                // String[] values = line.split(",");
                // schedule.add(Arrays.asList(values));
                // That split seemingly doesn't work at times for certain empty columns, so I'll
                // write my own
                ArrayList<String> values = new ArrayList<>();
                int i = 0;
                String value = "";
                while (i < line.length()) {
                    if (line.charAt(i) == ',') {
                        values.add(value);
                        value = "";
                    } else {
                        value = value + line.charAt(i);
                    }
                    i++;
                }
                values.add(value);

                schedule.add(values);
            }

            br.close();
        } catch (Exception e) {
            System.out.println("Failed to load data!");
            e.printStackTrace();
        }
    }

    private static long parseTime(String time) {
        if (time.length() == 0)
            return -1;
        // Example date:
        // 2025-03-01T02:01:00+01:00
        // Remove up to and including the T
        // Remove the + and after
        String t = time.substring(time.indexOf("T") + 1);
        t = t.substring(0, t.indexOf("+"));

        String[] split = t.split(":");

        // If the date is the 2nd of march, we need to add 24 * 3600
        int additionalHours = 0;
        if (time.substring(0, time.indexOf("T")).split("-")[2].equals("02"))
            additionalHours = 24;

        return (Integer.parseInt(split[0]) + additionalHours) * 3600 + Integer.parseInt(split[1]) * 60
                + Integer.parseInt(split[2]);
    }

    private static void loadTrains() {
        trains = new ArrayList<>();

        for (int i = 1; i < schedule.size() - 1; i++) {
            List<String> data = schedule.get(i);
            // service type, company, id, station id, station name, arrival time, departure
            // time, planned platform

            // If we aren't departing from this station, skip it
            if (data.get(6).length() == 0)
                continue;

            String serviceType = data.get(0);
            String serviceCompany = data.get(1);
            int id = Integer.parseInt(data.get(2));
            String departureStation = data.get(3);
            String departureStationName = data.get(4);
            // long arrivalTime = parseTime(data.get(5));
            long departureTime = parseTime(data.get(6));

            String stationPlatform = data.get(7);
            String stationArrivalPlatform = schedule.get(i + 1).get(7);
            // For some of the Schiphol trains, they add a -Jan or -May to the platform, if
            // that's the case just nuke it
            if (stationPlatform.contains("-")) {
                stationPlatform = stationPlatform.substring(0, stationPlatform.indexOf("-"));
            }
            if (stationArrivalPlatform.contains("-")) {
                stationArrivalPlatform = stationArrivalPlatform.substring(0, stationArrivalPlatform.indexOf("-"));
            }

            // The arrival time is the next line's arrival time
            long arrivalTime = parseTime(schedule.get(i + 1).get(5));
            String destinationStation = schedule.get(i + 1).get(3);
            String destinationStationName = schedule.get(i + 1).get(4);
            Train train = new Train(id, serviceType, serviceCompany, departureStation, destinationStation,
                    departureStationName, destinationStationName,
                    stationPlatform, stationArrivalPlatform, departureTime, arrivalTime);
            trains.add(train);
        }
    }

    private static void loadStations() {
        stations = new HashMap<>();
        stationIDs = new HashMap<>();

        for (Train train : trains) {
            String departureStation = train.getDepartureStation().toLowerCase();
            if (stations.containsKey(departureStation)) {
                Station station = stations.get(departureStation);
                station.getDepartures().add(train);
            } else {
                Station station = new Station(departureStation);
                station.getDepartures().add(train);
                stations.put(departureStation, station);
            }
        }

        // Now we must name the stations
        // Note that all of the lookups are lowercase, the station's data is still
        // capitalized correctly
        for (int i = 1; i < schedule.size(); i++) {
            List<String> data = schedule.get(i);
            String id = data.get(3).toLowerCase();
            String name = data.get(4).toLowerCase();
            if (stations.containsKey(id)) {
                Station s = stations.get(id);
                s.setName(data.get(4));
            } else { // This will happen if trains only go here, but don't leave (i.e. a few
                     // international stations)
                Station s = new Station(id);
                s.setName(data.get(4));
                stations.put(id, s);
            }

            // While we're at it, let's map the station id to its name
            if (!stationIDs.containsKey(name))
                stationIDs.put(name, id);
        }
    }

    public static Station getStationByName(String name) {
        String id = stationIDs.get(name.toLowerCase());
        return stations.get(id);
    }

    public static Station getStationByID(String id) {
        return stations.get(id.toLowerCase());
    }

    public static String input(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    private static void getData() {
        startID = "";
        endID = "";

        while (startID.equals("")) {
            String answer = input("Enter the departure station (id or name): ").toLowerCase();
            if (stationIDs.containsKey(answer)) {
                startID = stationIDs.get(answer);
                continue;
            } else {
                if (stations.containsKey(answer)) {
                    startID = answer;
                    continue;
                }
            }
        }
        while (endID.equals("")) {
            String answer = input("Enter the destination station (id or name): ").toLowerCase();
            if (stationIDs.containsKey(answer)) {
                endID = stationIDs.get(answer);
                if (endID.equals(startID)) {
                    endID = "";
                    System.out.println("Don't enter the same place man");
                }
                continue;
            } else {
                if (stations.containsKey(answer)) {
                    endID = answer;
                    if (endID.equals(startID)) {
                        endID = "";
                        System.out.println("Don't enter the same place man");
                    }
                    continue;
                }
            }
        }

        System.out.println("\nLoaded trip: " + stations.get(startID).getName() + " (" + startID.toUpperCase() + ") --> "
                + stations.get(endID).getName() + " (" + endID.toUpperCase() + ")");
    }

    private static String formatTime(long time) {
        return ((time / 3600) % 24) + String.format(":%02d", (time / 60) % 60);
    }

    static record Element(Station station, int cost, int lastTrainID, String platform, int trips, int index)
            implements Comparable<Element> {
        public int compareTo(Element other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    private static void search(Station station, long time, int maxTrips) {
        HashSet<Station> s = new HashSet<>();
        PriorityQueue<Element> q = new PriorityQueue<>();
        HashMap<Station, Integer> costs = new HashMap<>();

        int[] parent = new int[trains.size()];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = -1;
        }

        q.add(new Element(station, 0, -1, null, 0, 0));
        costs.put(station, 0);
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.station.getID().equals(endID)) {
                // Trace the trains back
                Stack<Train> path = new Stack<>();

                int i = e.index();
                while (parent[i] != -1) {
                    Train train = trains.get(i);
                    path.push(train);
                    i = parent[i];
                }

                ArrayList<Train> finalPath = new ArrayList<>();
                while (!path.isEmpty()) {
                    finalPath.add(path.pop());
                }

                Route route = new Route(e.trips());
                for (Train train : finalPath) {
                    route.add(train, null, null);
                }

                // Duration should be from the first train leaving, to the last train arriving
                route.increment(e.cost());
                route.decrement(route.path.get(0).getDepartureTime() - startTime);

                route.setDepartureStation(station);
                route.setArrivalStation(getStationByID(endID));

                routes.add(route);

                return;
            }

            s.add(e.station);

            // Get all of the next trains in the alloted maxWaitTime window
            long currentTime = time + e.cost();
            for (Train train : e.station().getDepartures(e.platform(), e.lastTrainID(), currentTime, minAbsoluteTime,
                    minWaitTime,
                    maxWaitTime)) {

                Station arrival = getStationByID(train.getArrivalStation());
                int cost = e.cost() + (int) train.getTripLength() + (int) train.timeToDeparture(currentTime);
                // TODO: that if statement breaks when: a faster path is found to some station
                // (see mt --> gn for 8:28, and this case Assen), but you can't take any more
                // trains out of there because you reached your trip limit, but that cost is
                // still saved and so it overrides this one which actually uses less trips:
                // if (!costs.containsKey(arrival) || costs.get(arrival) > cost) {
                if (true) {
                    boolean newTrip = train.getID() != e.lastTrainID();
                    int numTrips = e.trips() + (newTrip ? 1 : 0);
                    if (numTrips > maxTrips)
                        continue;

                    q.add(new Element(arrival, cost, train.getID(), train.getArrivalPlatform(), numTrips,
                            trains.indexOf(train)));
                    costs.put(arrival, cost);

                    parent[trains.indexOf(train)] = e.index();
                }
            }
        }
    }

    private static void generateRoutes() {
        routes = new ArrayList<>();

        // Limit to the number of trips
        // for (int k = 1; k < 5; k++) {
        // search(stations.get(startID), startTime, k);
        // }
        search(stations.get(startID), startTime, 2);

        // Now if any route dominates another, remove the dominated route
        for (int i = 0; i < routes.size(); i++) {
            for (int j = 0; j < routes.size(); j++) {
                if (i == j)
                    continue;

                Route a = routes.get(i);
                Route b = routes.get(j);
                if (a.getDuration() <= b.getDuration() && a.getTrips() <= b.getTrips()) {
                    // Remove b
                    routes.remove(b);
                    j--;
                }
            }
        }
    }

    private static void run() {
        scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n");
            getData();
            String departureTime = input("Enter your departure time (hh:mm): ");
            String[] t = departureTime.split(":");
            startTime = Integer.parseInt(t[0]) * 3600 + Integer.parseInt(t[1]) * 60;

            generateRoutes();
            for (Route route : routes) {
                System.out.println(route);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Loading data...");
        long startTime = System.nanoTime();

        loadSchedule();
        loadTrains();
        loadStations();

        long duration = System.nanoTime() - startTime;
        System.out.println("It took " + duration / 1_000_000 + " ms to load.");
        System.out.print("\nWelkom bij de officiële NS routeplanner!");

        run();
    }
}
