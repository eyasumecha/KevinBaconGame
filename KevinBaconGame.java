/**
 * @author Eyasu Lemma, CS 10, 2020 winter
 */

import org.bytedeco.opencv.opencv_text.IntDeque;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class KevinBaconGame {
    BufferedReader actorFile;  //my actor and ID file
    BufferedReader movieFile;   // my movie and ID file
    BufferedReader movieActorFile;  //my movie and actors set file
    AdjacencyMapGraph<String, Set<String>> movieMap;  //my main graph of actors as edges
    Map<String, String> movieID;    //my movie and ID map
    Map<String, String> actorID;    //my actor and ID map
    Map<String, Set<String>> actorMovie;   //my movie and actors set map
    String source, character;  // source and character parameters
    AdjacencyMapGraph<String, Set<String>> pathGraph;  //shortest path graph
    int size;
    HashSet<String> universe = new HashSet<>();  //vertices that have edges with each other
    double averageSeparation;
    char[] cArray;

    public KevinBaconGame() throws IOException {
        this.actorFile = new BufferedReader(new FileReader("actors.txt"));
        this.movieFile = new BufferedReader(new FileReader("movies.txt"));
        this.movieActorFile = new BufferedReader(new FileReader("movie-actors.txt"));
        movieMap = new AdjacencyMapGraph<>();
        movieID = new HashMap<>();
        actorID = new HashMap<>();
        actorMovie = new HashMap<>();

        String lines;

        while ((lines = this.actorFile.readLine()) != null) { //read the file till it is null
            String[] pieces = lines.split("\\|");  //split it at |
            actorID.put(pieces[0], pieces[1]);   //create and map my actor ID
        }

        while ((lines = this.movieFile.readLine()) != null) {  //read the file till it is null
            String[] pieces = lines.split("\\|");
            movieID.put(pieces[0], pieces[1]); //split acr0ss that line and create the movie map and initialize my movie and actor set map
            actorMovie.put(pieces[1], new HashSet<>());

        }

        while ((lines = this.movieActorFile.readLine()) != null) { //movie as key and actors that were in that movie as sets
            String[] pieces = lines.split("\\|");  // split the lines at | and use it to create the movie to actors set map
            Set<String> temp = actorMovie.get(movieID.get(pieces[0]));
            temp.add(actorID.get(pieces[1]));
            actorMovie.put(movieID.get(pieces[0]), temp);
        }
        this.actorFile.close();  //close all files
        this.movieFile.close();
        this.movieActorFile.close();


        for (String s : actorID.keySet()) {  //using actor ID create the vertices for the graph
            movieMap.insertVertex(actorID.get(s));
        }

        for (String s : actorMovie.keySet()) { //iterating through the movies
            for (String i : actorMovie.get(s)) { //finding a set of actors and iterating through them
                for (String u : actorMovie.get(s)) {// iterating through the same set of actors
                    if (!i.equals(u)) {   // iterate through each and find common actors who acted together and and create an edge and label between them
                        if (movieMap.hasEdge(i, u)) {
                            Set<String> temp = movieMap.getLabel(i, u);
                            temp.add(s);
                            movieMap.insertDirected(i, u, temp);
                        } else {
                            Set<String> temp = new HashSet<>();
                            temp.add(s);
                            movieMap.insertDirected(i, u, temp);
                        }

                    }
                }

            }
        }

        System.out.println("Commands:" + "\n" + "c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" +
                "d <low> <high>: list actors sorted by degree, with degree between low and high\n" +
                "i: list actors with infinite separation from the current center\n" +
                "p <name>: find path from <name> to current center of the universe\n" +
                "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" +
                "u <name>: make <name> the center of the universe\n" +
                "q: quit game");

        source = "Kevin Bacon"; //set Kevin Bacon as default source

        pathGraph = GraphLib.bfs(movieMap, source);  //find my shortest path graph

        for (String v : pathGraph.vertices()) {  //create a set of vertices with connection with the source
            universe.add(v);
        }

        size = movieMap.numVertices() - 1 - GraphLib.missingVertices(movieMap, pathGraph).size(); //find size

        averageSeparation = GraphLib.averageSeparation(pathGraph, "Kevin Bacon");  //calculate average separation

        System.out.println(source +" is now the center of the acting universe, connected to " + size + " actors with the average separation " + averageSeparation + "\n" + "\n" + source + " game >\n");
        Scanner in = new Scanner(System.in);


        do {
            String line, line1 = null; //str is the one we print, line1 is the one we compare as actor na
            line = in.nextLine();

            if(line != null){
                cArray = line.toCharArray();
                character = String.valueOf(cArray[0]);

                if(cArray.length == 1 && (!character.equals("i") && !character.equals("q"))){ //read the first character while it is not null and check if it matches
                    System.out.println("Please enter according to the instructions");
                    continue;// the single character inputs or not
                }
                else if(cArray.length > 1) {
                        line1 = new String(cArray, 2, cArray.length - 2);  // else combine array into word
                }
            }


            if (character.equals("p") && line1 != null) {

                if(!movieMap.hasVertex(line1)){
                    System.out.println("enter a valid person");
                }

                else if(line1.equals(source)){
                    System.out.println("input already center of graph. Put in new input");
                }
                else {
                    ArrayList<String> path = GraphLib.getPath(pathGraph, line1);  //find shortest path between line1 and source
                    if(path.size() == 0){
                        System.out.println("The two are not connected");
                    }
                    else{
                        size = path.size() - 1;   //find the path's size

                        String str = line1 + "'s number is " + size + "\n";   //write out the string
                        for (int a = 0; a < size; a++) {
                            str += path.get(a) + " appeared in " + movieMap.getLabel(path.get(a), path.get(a + 1)) + " with " + path.get(a + 1) + "\n";
                        }
                        str += source + " game >";
                        System.out.println(str);
                    }
                }
            }

            if (character.equals("c") && line1 != null) {
                ArrayList<String> universeCenter = new ArrayList<>(); //create my assistance map and list
                HashMap<String, Double> universeMap = new HashMap<>();

                for (String x : movieMap.vertices()) {
                    if (universe.contains(x)) {
                        double y = GraphLib.averageSeparation(GraphLib.bfs(movieMap, x), x);  //calculate the average separation for each vertice
                        universeMap.put(x, y);
                        universeCenter.add(x);
                    }
                }

                universeCenter.sort(Comparator.comparingDouble(universeMap::get)); //sort it using my map

                int number = Integer.parseInt(line1);  //find my integer value

                ArrayList<String> listTopBottom = new ArrayList<>();  //depending on its value, append from the end or the beginning
                if (number > 0) {
                    for (int i = 0; i < number; i++) {
                        listTopBottom.add(universeCenter.get(i));
                    }
                } else {
                    for (int i = universeCenter.size() - 1; i > universeCenter.size() + number - 1; i--) {
                        listTopBottom.add(universeCenter.get(i));
                    }
                }
                System.out.println("people sorted by separation are\n");
                System.out.println(listTopBottom);
                System.out.println("\n" + source + " game>\n");
            }

            if (character.equals("d")) {
                if (line1 != null) {
                    String[] pieces = line1.split(" ");  //if condition satisfied then parse the line and find high and low
                    int low = Integer.parseInt(pieces[0]);
                    int high = Integer.parseInt(pieces[1]);

                    ArrayList<String> actors = new ArrayList<>();  //list for actors
                    for (String i : movieMap.vertices()) {  // add vertices according to low and high and their inDegree values
                        if (movieMap.inDegree(i) >= low && movieMap.inDegree(i) <= high) {
                            actors.add(i);
                        }
                    }
                    actors.sort(Comparator.comparingInt((String s) -> movieMap.inDegree(s)));  //sort using our Map created
                    if(!actors.isEmpty()){
                        System.out.println("people with degrees with in that range are:\n");
                        System.out.println(actors);
                    }

                    else{
                        System.out.println("no people with degrees in that specified range");
                    }

                    System.out.println("\n" + source + " game>\n");
                }
            }


            if (character.equals("i")) {
                System.out.println("people that are not connected to the center are\n");
                System.out.println(GraphLib.missingVertices(movieMap, pathGraph));  //return vertices that are not connected to the source
                System.out.println("\n" + source + " game>\n");
            }

            if (character.equals("s")) {
                ArrayList<String> centerDouble = new ArrayList<>(); //my list and map for helping with the non-infinite separation from the center
                HashMap<String, Integer> centerMap = new HashMap<>();

                if (line1 != null) {
                    String[] pieces = line1.split(" "); // find my low and high integer
                    int low = Integer.parseInt(pieces[0]);
                    int high = Integer.parseInt(pieces[1]);

                    for (String i : pathGraph.vertices()) {//find my separation fo the non infinite actors
                        if (!i.equals(source)) {
                            int y = GraphLib.getPath(pathGraph, i).size() - 1;
                            centerMap.put(i, y);
                            centerDouble.add(i);
                        }
                    }
                    centerDouble.sort(Comparator.comparingInt(centerMap::get));  //sort the list according to their separation

                    ArrayList<String> centerString = new ArrayList<>();  //and then add the ones that fall with in the range
                    for (String d : centerMap.keySet()) {
                        if (centerMap.get(d) >= low && centerMap.get(d) <= high) {
                            centerString.add(d);
                        }
                    }
                    System.out.println("people that are non infinite and who separation falls with in the range are\n");
                    System.out.println(centerString);
                    System.out.println("\n" + source + " game>\n");
                }
            }

            if (character.equals("u") && line1 != null) {
                if (movieMap.hasVertex(line1)) {
                    source = line1;
                    pathGraph = GraphLib.bfs(movieMap, source);
                    size = movieMap.numVertices() - 1 - GraphLib.missingVertices(movieMap, pathGraph).size();

                    averageSeparation = GraphLib.averageSeparation(pathGraph, source);
                    System.out.println(source + " is now the center of the acting universe, connected to " + size + " people with the average separation " + averageSeparation + "\n" + source + " game >\n");


                }
            }
        }
            while (!character.equals("q")) ;
        }






    public static void main(String[] args) throws IOException {
        KevinBaconGame game = new KevinBaconGame();
    }
}
