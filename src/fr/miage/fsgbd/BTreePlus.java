package fr.miage.fsgbd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel, Nicolas Parizet, Thomas Delmare, Marie-Celeste SANCHEZ
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Map<Integer, Integer> mapCSV = new HashMap<Integer, Integer>();

    // Collection des noeuds feuilles ordonnées dans l'arbre
    public ArrayList<Noeud<Type>> sheetLink = new ArrayList<Noeud<Type>>();

    //Feuille de l'arbre affiché dans l'interface pour vérifier les suivantes => initialisé par la 1er feuille de l'arbre
    private Noeud<Type> currentSheet;

    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    public String getKeysNextSheet(){
        String keys = "";
        int length = currentSheet.keys.size();
        for(int i = 0; i < length ; i++){
            keys += currentSheet.keys.get(i) +" ";
        }
        this.currentSheet = currentSheet.getNextSheet();
        return keys;
    }


    public void createSheetLink() {
        // Récupére le noeud le plus haut de l'arbre
        Noeud<Type> n = racine;
        boolean process = true;

        // Récupère la première feuille de l'arbre
        n = goFirstSheetOfNoeud(n);

        //Définie premier noeud de l'arbre
        this.currentSheet = n;

        // Ajout de la première feuille de l'arbre dans la liste
        sheetLink.add(n);        
        while (process) {
            if (!n.fils.isEmpty()){
                n = goFirstSheetOfNoeud(n);
            }
            if((n = checkNext(n)) != null){
                if (n.fils.isEmpty()){
                    //Set la variable de la feuille précédente pour que celui-ci pointe vers le noeud actuel, soit la feuille suivant !
                    sheetLink.get(sheetLink.size()-1).setNextSheet(n);;
                    sheetLink.add(n);
                    /*n.keys.forEach((key) -> {
                        System.out.println(" Feuille " + key); 
                    });*/
                }
            } else {
                process = false;
            }
        }
        //checkSheet();
    }

    // Fonction permettant de remonter l'arbre jusqu'a obtention d'un noeud suivant
    public Noeud<Type> checkNext(Noeud<Type> n) {
        while(n.getNoeudSuivant() == null){
            n = n.getParent();
            if(n.getParent() == null){
                return null;
            }
        }
        n = n.getNoeudSuivant();
        if(!n.fils.isEmpty()){
            n = goFirstSheetOfNoeud(n);
        }
        return n;
    }

    
    //Fonction permettant d'aller récupérer la 1er feuille de l'arbre
    public Noeud<Type> goFirstSheetOfNoeud(Noeud<Type> n) {
        while (!n.fils.isEmpty()) {
            n = n.fils.get(0);
        }
        return n;
    }

    //Fonction pour vérifier la liste des feuilles dans l'ordre
    public void checkSheet() {
        sheetLink.forEach((n) -> {
            n.keys.forEach((key) -> {
                System.out.println(" => " + key); 
            });
        });
    }


    /**
     * Méthode récursive permettant de récupérer tous les noeuds
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode bArbreToJTree() {
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder txt = new StringBuilder();
        for (Type key : root.keys)
            txt.append(key.toString()).append(" ");

        DefaultMutableTreeNode racine2 = new DefaultMutableTreeNode(txt.toString(), true);
        for (Noeud<Type> fil : root.fils)
            racine2.add(bArbreToJTree(fil));

        return racine2;
    }

    public boolean addValeur(Type valeur) {
        // System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }

    public boolean addValeurFromCSV(Type valeur, int numRow) {
        // System.out.println("Ajout de la valeur : " + valeur.toString() + " ayant
        // comme pointeur " + numRow);
        mapCSV.put(Integer.parseInt(valeur.toString()), numRow);
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }

    public void checkMapCSV() {
        for (Map.Entry<Integer, Integer> entry : mapCSV.entrySet()) {
            System.out.println("Clef " + entry.getKey() + " - Valeur " + entry.getValue());
        }
    }

    public void removeValeur(Type valeur) {
        System.out.println("Retrait de la valeur : " + valeur.toString());
        if (racine.contient(valeur) != null) {
            Noeud<Type> newRacine = racine.removeValeur(valeur, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }

    // RECHERCHE

    public double searchForCsvValueViaMap(Integer wantedValue) {
        long startTime = System.nanoTime();
        Integer foundValue = mapCSV.get(wantedValue);
        String lineToFind = "";

        try (Stream<String> lines = Files.lines(Paths.get("DBProject.csv"))) {
            lineToFind = lines.skip(foundValue-1).findFirst().get();
        }
        catch(IOException e1) {
            e1.printStackTrace();
            return 0;
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // System.out.println("searchForCsvValueViaMap found : " + foundValue);
        // System.out.println("searchForCsvValueViaMap duration : " + duration);

        return duration;
    }

    public double searchForCsvValueViaFile(Integer wantedValue, String csvPath) {
        long startTime = System.nanoTime();
        String row;
        int numRow = 0;

        if(csvPath == "" || csvPath == null){
            csvPath = "DBProject.csv";
        }

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(csvPath));
            while ((row = csvReader.readLine()) != null) {
                numRow++;
                String[] data = row.split(",");

                if (!data[0].equals("id")) {
                    if (wantedValue == Integer.parseInt(data[0])) {
                        // System.out.println(
                                // "Data Row " + numRow + " : " + data[0] + " " + data[1] + " " + data[2] + " " + data[3]);

                        long endTime = System.nanoTime();
                        long duration = (endTime - startTime);

                        // System.out.println("searchForCsvValueViaFile found : " + numRow);
                        // System.out.println("searchForCsvValueViaFile duration : " + duration);

                        // On retourne la durée d'exécution
                        return duration;
                    }

                }
            }
            csvReader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            return 0;
        }
        return 0;
    }

    public HashMap<String,Double> lancerTestRecherche() {
        ArrayList<Integer> listeRecherches = genererNombresAleatoires();
        HashMap<String,Double> timeResults = new HashMap<String,Double>();
        
        double totalFile = 0;
        double totalMap = 0;

        double tempDurationFile;
        double tempDurationMap;

        double maxCSvValueViaFile = 0;
        double maxCSvValueViaMap = 0;

        // Ca pourrait être géré d'une meilleure façon
        double minCSvValueViaFile = 0;
        double minCSvValueViaMap = 0;

        double moyenneCsvValueViaFile = 0;
        double moyenneCsvValueViaMap = 0;

        for (int i = 0; i < listeRecherches.size(); i++) {
            tempDurationFile = searchForCsvValueViaFile(listeRecherches.get(i), "");
            tempDurationMap = searchForCsvValueViaMap(listeRecherches.get(i));

            if(i == 0){
                maxCSvValueViaFile = tempDurationFile;
                maxCSvValueViaMap = tempDurationMap;
                minCSvValueViaFile = tempDurationFile;
                minCSvValueViaMap = tempDurationMap;
            }
            else{
                // On regarde si la durée est plus grande que celle connue
                if(tempDurationFile > maxCSvValueViaFile){
                    maxCSvValueViaFile = tempDurationFile;
                }
                if(tempDurationMap > maxCSvValueViaMap){
                    maxCSvValueViaMap = tempDurationMap;
                }

                // On regarde si la durée est plus petite que celle connue
                if(tempDurationFile < minCSvValueViaFile){
                    minCSvValueViaFile = tempDurationFile;
                }
                if(tempDurationMap < minCSvValueViaMap){
                    minCSvValueViaMap = tempDurationMap;
                }
            }

            totalMap += tempDurationMap;
            totalFile += tempDurationFile;

        }

        // On marque la durée moyenne de traitement pour chacune des opérations
        moyenneCsvValueViaFile = totalFile / listeRecherches.size();
        moyenneCsvValueViaMap = totalMap / listeRecherches.size();

        /*System.out.println("\nPlus petite valeur pour Map : " + minCSvValueViaMap + " ms");
        System.out.println("Plus grande valeur pour Map : " + maxCSvValueViaMap + " ms");
        System.out.println("Moyenne pour Map : " + moyenneCsvValueViaMap + " ms");

        System.out.println("\nPlus petite valeur pour File : " + minCSvValueViaFile + " ms");
        System.out.println("Plus grande valeur pour File : " + maxCSvValueViaFile + " ms");
        System.out.println("Moyenne pour File : " + moyenneCsvValueViaFile + " ms");*/

        timeResults.put("minMap", minCSvValueViaMap);
        timeResults.put("maxMap", maxCSvValueViaMap);
        timeResults.put("avgMap", moyenneCsvValueViaMap);
        timeResults.put("minCSV", minCSvValueViaFile);
        timeResults.put("maxCSV", maxCSvValueViaFile);
        timeResults.put("avgCSV", moyenneCsvValueViaFile);

        return timeResults;
    }

    public ArrayList<Integer> genererNombresAleatoires() {
        ArrayList<Integer> numbersList = new ArrayList<>();
        Random rand = new Random();
        Collection<Integer> valuesToRandom = mapCSV.keySet();
        ArrayList<Integer> list = new ArrayList<>(valuesToRandom); 

        for (int i = 0; i < 100; i++) {
            numbersList.add(list.get(rand.nextInt(list.size())));
        }

        return numbersList;
    }
}
