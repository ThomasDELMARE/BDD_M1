package fr.miage.fsgbd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Map<Integer, Integer> mapCSV = new HashMap<Integer, Integer>();

    // Collection des noeuds feuilles ordonnées dans l'arbre
    public ArrayList<Noeud<Type>> sheetLink = new ArrayList<Noeud<Type>>();

    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    public void createSheetLink() {
        Noeud<Type> sheet = null;
        // Récupére le noeaud le plus haut de l'arbre
        Noeud<Type> n = racine;
        int i = 0;
        boolean process = true;

        while (process) {
            // Récupère la première feuille de l'arbre
            n = goFirstSheetOfNoeud(n);
            // Ajout de la première feuille de l'arbre dans la liste
            sheetLink.add(n);

            while ((n = n.getNoeudSuivant()) != null) {
                sheetLink.add(n);
            }

        }
    }

    // Fonction recursive permettant de remonter l'arbre jusqu'a obtention d'un
    // noeud suivant
    public Noeud<Type> checkNext(Noeud<Type> n) {
        if (n.getNoeudSuivant() != null) {
            return n;
        } else {
            n.getParent();
            // si on remonte jusqu'à la racine => stop => on a fini de traverser l'arbre
            if (n == racine) {
                return null;
            }
            checkNext(n);
        }
        // ne devrait jamais passer ici
        return null;
    }

    public Noeud<Type> goFirstSheetOfNoeud(Noeud<Type> r) {
        while (!r.fils.isEmpty()) {
            r = r.fils.get(0);
        }
        return r;
    }

    public void addSheetsOfNoeud(Noeud<Type> r) {
        sheetLink.add(r);
    }

    /**
     * 
     * int nb = 0;
     * sheetLink.forEach((sh) -> {
     * sh.keys.forEach((key) -> {
     * System.out.println(nb + " : " + key);
     * });
     * });
     * 
     */

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

                        // On retourne l'Id
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

    public void lancerTestRecherche() {
        ArrayList<Integer> listeRecherches = genererNombresAleatoires();
        
        double totalFile = 0;
        double totalMap = 0;

        double tempDurationFile = 0;
        double tempDurationMap = 0;

        double maxCSvValueViaFile = 0;
        double maxCSvValueViaMap = 0;

        // Ca pourrait être géré d'une meilleure façon
        double minCSvValueViaFile = 1000000;
        double minCSvValueViaMap = 1000000;

        double moyenneCsvValueViaFile = 0;
        double moyenneCsvValueViaMap = 0;

        for (int i = 0; i < listeRecherches.size(); i++) {
            tempDurationFile = searchForCsvValueViaFile(listeRecherches.get(i), "");
            tempDurationMap = searchForCsvValueViaMap(listeRecherches.get(i));

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

            totalMap += tempDurationMap;
            totalFile += tempDurationFile;

        }

        // On marque la durée moyenne de traitement pour chacune des opérations
        moyenneCsvValueViaFile = totalFile / listeRecherches.size();
        moyenneCsvValueViaMap = totalMap / listeRecherches.size();

        System.out.println("\nPlus petite valeur pour Map : " + minCSvValueViaMap);
        System.out.println("Plus grande valeur pour Map : " + maxCSvValueViaMap);
        System.out.println("Plus petite valeur pour Map : " + moyenneCsvValueViaMap);

        System.out.println("\nPlus petite valeur pour File : " + minCSvValueViaFile);
        System.out.println("Plus grande valeur pour File : " + maxCSvValueViaFile);
        System.out.println("Plus petite valeur pour File : " + moyenneCsvValueViaFile);

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
