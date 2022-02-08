package fr.miage.fsgbd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Map<Integer, Integer> mapCSV = new HashMap<Integer,Integer>();
    
    //Collection des noeuds feuilles ordonnées dans l'arbre
    public ArrayList<Noeud<Type>> sheetLink = new ArrayList<Noeud<Type>>();


    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    public void createSheetLink(){
        Noeud<Type> sheet = null;
        // Récupére le noeaud le plus haut de l'arbre
        Noeud<Type> n = racine;
        int i = 0;
        boolean process = true;

        while(process){
            // Récupère la première feuille de l'arbre
            n = goFirstSheetOfNoeud(n);
            // Ajout de la première feuille de l'arbre dans la liste
            sheetLink.add(n);

            while((n = n.getNoeudSuivant()) != null){
                sheetLink.add(n);
            }


            
        }
    }

    // Fonction recursive permettant de remonter l'arbre jusqu'a obtention d'un noeud suivant
    public Noeud<Type> checkNext(Noeud<Type> n){
        if(n.getNoeudSuivant() != null){
            return n;
        } else {
            n.getParent();
            //si on remonte jusqu'à la racine => stop => on a fini de traverser l'arbre
            if(n == racine){
                return null;
            }
            checkNext(n);
        }
        //ne devrait jamais passer ici
        return null;
    }

    public Noeud<Type> goFirstSheetOfNoeud(Noeud<Type> r){
        while(!r.fils.isEmpty()){
            r = r.fils.get(0);
        }
        return r;
    }

    public void addSheetsOfNoeud(Noeud<Type> r){
        sheetLink.add(r);
    }

/**
 *             
            int nb = 0;
            sheetLink.forEach((sh) -> {
                sh.keys.forEach((key) -> {
                    System.out.println(nb + " : " + key);            
                });
            });
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
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }

    public boolean addValeurFromCSV(Type valeur, int numRow) {
        System.out.println("Ajout de la valeur : " + valeur.toString() + " ayant comme pointeur " + numRow );
        mapCSV.put(Integer.parseInt(valeur.toString()), numRow);
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }

    public void checkMapCSV(){
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
}
