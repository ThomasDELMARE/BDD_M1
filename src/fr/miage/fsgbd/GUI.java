package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 */
public class GUI extends JFrame implements ActionListener {
    TestInteger testInt = new TestInteger();
    BTreePlus<Integer> bInt;
    private JButton buttonClean, buttonRemove, buttonLoad, buttonSave, buttonAddMany, buttonAddItem, buttonRefresh, buttonAddFromCSV, buttonNextSheet, buttonTestProcess;
    private JTextField txtNbreItem, txtNbreSpecificItem, txtU, txtFile, removeSpecific, txtCSV, txtNextSheet, txtMinMap, txtMaxMap, txtAvgMap, txtMinCSV, txtMaxCSV, txtAvgCSV;
    private final JTree tree = new JTree();
    final JPanel panelWarning = new JPanel();

    public GUI() {
        super();
        build();
    }

    public void getKeysNextSheet(){
        this.txtNextSheet.setText(bInt.getKeysNextSheet()); ;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonLoad || e.getSource() == buttonClean || e.getSource() == buttonSave || e.getSource() == buttonRefresh) {
            if (e.getSource() == buttonLoad) {
                BDeserializer<Integer> load = new BDeserializer<Integer>();
                bInt = load.getArbre(txtFile.getText());
                if (bInt == null)
                    System.out.println("Echec du chargement.");

            } else if (e.getSource() == buttonClean) {
                if (Integer.parseInt(txtU.getText()) < 2)
                    System.out.println("Impossible de cr?er un arbre dont le nombre de cl?s est inf?rieur ? 2.");
                else
                    bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);
            } else if (e.getSource() == buttonSave) {
                BSerializer<Integer> save = new BSerializer<Integer>(bInt, txtFile.getText());
            }else if (e.getSource() == buttonRefresh) {
                tree.updateUI();
            }
        } else {
            // Initialisation d'un BTree si null
            if (bInt == null) 
                bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);

            if (e.getSource() == buttonAddMany) {
                for (int i = 0; i < Integer.parseInt(txtNbreItem.getText()); i++) {
                    int valeur = (int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
                    boolean done = bInt.addValeur(valeur);
                    bInt.createSheetLink();
					/*
					  On pourrait forcer l'ajout mais on risque alors de tomber dans une boucle infinie sans "r?gle" faisant sens pour en sortir

					while (!done)
					{
						valeur =(int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
						done = bInt.addValeur(valeur);
					}
					 */
                }
            }
            //Fonction pour lire un fichier CSV
            else if (e.getSource() == buttonAddFromCSV){
                String row;
                int numRow = 0;
                try {
                    BufferedReader csvReader = new BufferedReader(new FileReader(txtCSV.getText()));
                    while((row = csvReader.readLine()) != null){
                        numRow ++;
                        String[] data = row.split(",");
                        
                        // System.out.println("Data Row " + numRow + " : " + data[0] + " " +  data[1] + " " + data[2] + " " +  data[3]);
                        // Gérer la row HEADER (ou enlever du CSV ??)   
                        if (data[0].toString().contentEquals("id")){ // => pas terrible en l'état
                        } 
                        else {
                            //Add data au Btree en tableau associatif : map[id(duCSV)] = pointeur(numRow)
                            bInt.addValeurFromCSV(Integer.parseInt(data[0]), numRow);
                        }
                    }

                    // bInt.checkMapCSV();
                    bInt.lancerTestRecherche();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                bInt.createSheetLink();
            }
            else if (e.getSource() == buttonAddItem) {
                if (!bInt.addValeur(Integer.parseInt(txtNbreSpecificItem.getText())))
                    System.out.println("Tentative d'ajout d'une valeur existante : " + txtNbreSpecificItem.getText());
                txtNbreSpecificItem.setText(
                        String.valueOf(
                                Integer.parseInt(txtNbreSpecificItem.getText()) + 2
                        )
                );

            } else if (e.getSource() == buttonRemove) {
                if(bInt.sheetLink.isEmpty()){
                    JOptionPane.showMessageDialog(panelWarning, "Create B tree first", "Warning",
                    JOptionPane.WARNING_MESSAGE);
                } else {
                    bInt.removeValeur(Integer.parseInt(removeSpecific.getText()));
                }
            } else if (e.getSource() == buttonNextSheet) {
                if(bInt.sheetLink.isEmpty()){
                    JOptionPane.showMessageDialog(panelWarning, "Create B tree first", "Warning",
                    JOptionPane.WARNING_MESSAGE);
                } else {
                    this.getKeysNextSheet();
                }
            } else if (e.getSource() == buttonTestProcess) {
                if(bInt.sheetLink.isEmpty()){
                    JOptionPane.showMessageDialog(panelWarning, "Create B tree first", "Warning",
                    JOptionPane.WARNING_MESSAGE);
                } else {
                    HashMap<String,Double> timeResults = new HashMap<String,Double>();
                    timeResults = bInt.lancerTestRecherche();
                    txtMinMap.setText(timeResults.get("minMap").toString() +" ms");
                    txtMaxMap.setText(timeResults.get("maxMap").toString() +" ms");
                    txtAvgMap.setText(timeResults.get("avgMap").toString() +" ms");
                    txtMinCSV.setText(timeResults.get("minCSV").toString() +" ms");
                    txtMaxCSV.setText(timeResults.get("maxCSV").toString() +" ms");
                    txtAvgCSV.setText(timeResults.get("avgCSV").toString() +" ms");
                }
            }
        }

        tree.setModel(new DefaultTreeModel(bInt.bArbreToJTree()));
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        tree.updateUI();
    }

    private void build() {
        setTitle("Indexation - B Arbre");
        setSize(760, 760);
        setLocationRelativeTo(this);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContentPane());
    }

    private JPanel buildContentPane() {
        GridBagLayout gLayGlob = new GridBagLayout();

        JPanel pane1 = new JPanel();
        pane1.setLayout(gLayGlob);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 2, 0);

        JLabel labelU = new JLabel("Nombre max de clefs par noeud (2m): ");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        pane1.add(labelU, c);

        txtU = new JTextField("4", 7);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 2;
        pane1.add(txtU, c);

        JLabel labelBetween = new JLabel("Nombre de clefs à ajouter:");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(labelBetween, c);

        txtNbreItem = new JTextField("10000", 7);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(txtNbreItem, c);


        buttonAddMany = new JButton("Ajouter n éléments aléatoires à l'arbre");
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddMany, c);

        JLabel labelSpecific = new JLabel("Ajouter une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelSpecific, c);

        txtNbreSpecificItem = new JTextField("50", 7);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtNbreSpecificItem, c);

        buttonAddItem = new JButton("Ajouter l'élément");
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddItem, c);

        JLabel labelRemoveSpecific = new JLabel("Retirer une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelRemoveSpecific, c);

        removeSpecific = new JTextField("54", 7);
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(removeSpecific, c);

        buttonRemove = new JButton("Supprimer l'élément n de l'arbre");
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRemove, c);

        JLabel labelFilename = new JLabel("Nom de fichier : ");
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelFilename, c);

        txtFile = new JTextField("arbre.abr", 7);
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtFile, c);

        buttonSave = new JButton("Sauver l'arbre");
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonSave, c);

        buttonLoad = new JButton("Charger l'arbre");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoad, c);

        buttonClean = new JButton("Reset");
        c.gridx = 2;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonClean, c);

        buttonRefresh = new JButton("Refresh");
        c.gridx = 2;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRefresh, c);

        //Q1 - Generer arbre depuis CSV
        buttonAddFromCSV = new JButton("Créer arbre depuis un fichier csv");
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddFromCSV, c);

        JLabel labelCSVFilename = new JLabel("Nom du CSV à ajouter : ");
        c.gridx = 0;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelCSVFilename, c);

        txtCSV = new JTextField("DBProject.csv", 7);
        c.gridx = 1;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtCSV, c);

        //Q1 - Chainer les feuilles 
        buttonNextSheet = new JButton("Feuille suivante");
        c.gridx = 2;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonNextSheet, c);

        JLabel labelNextSheet = new JLabel("Valeur de la feuille actuelle : ");
        c.gridx = 0;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(labelNextSheet, c);

        txtNextSheet = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(txtNextSheet, c);

        //Q2 - Comparaison vitesse exécution
        buttonTestProcess = new JButton("Procéder au un test de vitesse de recherche via Map ou via recherche dans CSV");
        c.gridx = 1;
        c.gridy = 11;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(buttonTestProcess, c);

        //Plus petite valeur pour Map
        JLabel labelMinMap = new JLabel("Valeur min Map : ");
        c.gridx = 0;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(labelMinMap, c);

        txtMinMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMinMap, c);

        //Plus petite valeur pour CSV
        JLabel labelMinCSV = new JLabel("Valeur min CSV : ");
        c.gridx = 2;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(labelMinCSV, c);

        txtMinCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(txtMinCSV, c);

        //Plus grande valeur pour Map
        JLabel labelMaxMap = new JLabel("Valeur max Map : ");
        c.gridx = 0;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelMaxMap, c);

        txtMaxMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMaxMap, c);

        //Plus grande valeur pour CSV
        JLabel labelMaxCSV = new JLabel("Valeur max CSV : ");
        c.gridx = 2;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelMaxCSV, c);

        txtMaxCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMaxCSV, c);

        //Moyenne des valeurs pour Map
        JLabel labelAvgMap = new JLabel("Valeur moyenne Map : ");
        c.gridx = 0;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelAvgMap, c);

        txtAvgMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtAvgMap, c);

        //Plus grande valeur pour CSV
        JLabel labelAvgCSV = new JLabel("Valeur moyenne CSV : ");
        c.gridx = 2;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelAvgCSV, c);

        txtAvgCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtAvgCSV, c);


        //Propriétés du panel
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 400;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.gridwidth = 4;   //2 columns wide
        c.gridx = 0;
        c.gridy = 20;



        JScrollPane scrollPane = new JScrollPane(tree);
        pane1.add(scrollPane, c);

        tree.setModel(new DefaultTreeModel(null));
        tree.updateUI();

        txtNbreItem.addActionListener(this);
        buttonAddItem.addActionListener(this);
        buttonAddMany.addActionListener(this);
        buttonLoad.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonRemove.addActionListener(this);
        buttonClean.addActionListener(this);
        buttonRefresh.addActionListener(this);
        buttonAddFromCSV.addActionListener(this);
        buttonNextSheet.addActionListener(this);
        buttonTestProcess.addActionListener(this);

        return pane1;
    }
}

