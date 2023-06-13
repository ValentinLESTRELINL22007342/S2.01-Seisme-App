package com.example.s201;

import com.gluonhq.maps.MapPoint;
import javafx.collections.FXCollections;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.Pair;

public  class  SeismeData { 

    @FXML
    private Label moyenne;

    private static ObservableList<XYChart.Data<String, Number>> DonneesScatterchart = FXCollections.observableArrayList();
    //Les données pour le barchart

    public static XYChart.Series<String, Number> SerieDonneesScatterchart = new XYChart.Series<String, Number>();
    //Contient toutes les données du barchart et permet l'évolution des données;

    private static ObservableList<PieChart.Data> DonneesCamembert = FXCollections.observableArrayList(
            new PieChart.Data("Segment 1", 30),
            new PieChart.Data("Segment 2", 20),
            new PieChart.Data("Segment 3", 50)
    );
    // Les données pour le camembert

    private static ObservableList<XYChart.Data<Number, Number>> DonneesLineChart = FXCollections.observableArrayList();
    // Les données pour la LineChart

    public static XYChart.Series<Number, Number> SerieDonneesLineChart = new XYChart.Series<Number, Number>();

    public static ObservableList<Pair<MapPoint,Double>> pointMapDonnees = FXCollections.observableArrayList();


    public static void prepDonneesScatterchart(LocalDate minDate, LocalDate maxDate,
                                               Double minIntensite, Double maxIntensite) throws ParseException {
        if (DonneesScatterchart.size() != 0)
            DonneesScatterchart.removeAll();
        ArrayList<Pair<LocalDate, Double>> donneesTemp = new ArrayList<>();
        for (int i=0; i < GestionDonneesCSV.getDonnees().size(); i+=1){
            if (GestionDonneesCSV.getDateDonnees().get(i) == null || GestionDonneesCSV.getIntensiteDonnees().get(i) == null) {
                continue;
            }
            else {
                if (GestionDonneesCSV.getIntensiteDonnees().get(i) <= 12 && GestionDonneesCSV.getIntensiteDonnees().get(i) >= 0){
                    LocalDate tempDate = GestionDonneesCSV.getDateDonnees().get(i);
                    Double tempIntensite = GestionDonneesCSV.getIntensiteDonnees().get(i);
                    donneesTemp.add(new Pair<>(tempDate, tempIntensite));
                }
                else
                   continue;
            }
        }
        //Trie de la liste de données pour permttre un affichage trié dans le graphique
        Collections.sort(donneesTemp, new Comparator<Pair<LocalDate, Double>>() {
            @Override
            public int compare(Pair<LocalDate, Double> pair1, Pair<LocalDate, Double> pair2) {
                return pair1.getKey().compareTo(pair2.getKey());
            }
        });
        for (Pair<LocalDate,Double> seisme : donneesTemp){
            if (seisme.getValue() >= minIntensite && seisme.getValue() <= maxIntensite) {
                assert seisme.getKey() != null;
                if (seisme.getKey().isAfter(minDate) || seisme.getKey().isEqual(minDate)
                        && seisme.getKey().isBefore(maxDate) || seisme.getKey().isEqual(maxDate)) {
                    DonneesScatterchart.add(new XYChart.Data<>(String.valueOf(seisme.getKey()),seisme.getValue()));
                }
            }
        }
    }

    //Sert à la visualisation des données pour le graphique en camembert.
    public void prepDonneesCamembert(List<List<String>> Donnees, int minIntensite, int maxIntensite) {
        if (DonneesCamembert.size() != 0)
            DonneesCamembert.removeAll();
        int count = 0;
        int totalIntensite = 0; // Ajout de cette variable pour calculer la somme des intensités

        for (int i = 0; i < Donnees.size(); i += 1) {
            int tempIntensite = Integer.valueOf(Donnees.get(i).get(Donnees.size() - 2));
            if (tempIntensite >= minIntensite && tempIntensite <= maxIntensite) {
                count++;
                totalIntensite += tempIntensite; // Ajout de l'intensité à la somme totale
            }
        }

        for (int i = 0; i < Donnees.size(); i += 1) {
            int tempIntensite = Integer.valueOf(Donnees.get(i).get(Donnees.size() - 2));
            if (tempIntensite >= minIntensite && tempIntensite <= maxIntensite) {
                double percentage = (double) count / Donnees.size() * 100;
                String categorie = "Catégorie " + (i + 1);
                DonneesCamembert.add(new PieChart.Data(categorie, percentage));
            }
        }

        // Calcul de la moyenne des intensités correspondant aux critères
        double moyenneIntensite = calculateMoyenneIntensite(Donnees, minIntensite, maxIntensite);

        // Mise à jour du texte du label moyenne avec la valeur calculée
        moyenne.setText(String.format("%.2f", moyenneIntensite));

    }

    //Récupère les données du graphique en camembert.
    public static ObservableList<PieChart.Data> getDonneesCamembert() {
        return DonneesCamembert;
    }
    private double calculateMoyenneIntensite(List<List<String>> Donnees, int minIntensite, int maxIntensite) {
        int count = 0;
        int sum = 0;

        for (int i = 0; i < Donnees.size(); i++) {
            int tempIntensite = Integer.parseInt(Donnees.get(i).get(Donnees.size() - 2));
            if (tempIntensite >= minIntensite && tempIntensite <= maxIntensite) {
                count++;
                sum += tempIntensite;
            }
        }

        if (count > 0) {
            return (double) sum / count;
        } else {
            return 0.0;
        }
    }


    public static void prepdonneesCourbe(List<List<String>> Donnees, Double minDate, Double maxDate,
                                         Double minIntensite, Double maxIntensite){
        if (DonneesLineChart.size() != 0)
            DonneesLineChart.removeAll();
        for (int i=0; i < Donnees.size(); i+=1) {
            String[] tempDate = Donnees.get(i).get(1).split("/+");
            Double tempAnnee = Double.valueOf(tempDate[0]);
            Double tempIntensité = Double.valueOf(Donnees.get(i).get(Donnees.get(i).size() - 2));
            if (tempIntensité >= minIntensite && tempIntensité <= maxIntensite
                    && tempAnnee >= minDate && tempAnnee <= maxDate) {
                DonneesLineChart.add(new XYChart.Data<>(tempAnnee, tempIntensité));
            }
        }
    }


    //Prépare les données pour les points de la carte
    public static void prepDonneesMap(LocalDate minDate, LocalDate maxDate, Double minIntensite, Double maxIntensite){
        if(pointMapDonnees.size()!= 0)
            pointMapDonnees.removeAll();
        for (int i=0; i < GestionDonneesCSV.getDonnees().size(); i+=1){
            if ((GestionDonneesCSV.getIntensiteDonnees().get(i) >= minIntensite && GestionDonneesCSV.getIntensiteDonnees().get(i) <= maxIntensite)
                    && (GestionDonneesCSV.getDateDonnees().get(i).isAfter(minDate) && GestionDonneesCSV.getDateDonnees().get(i).isBefore(maxDate))){
                if (GestionDonneesCSV.getPosGPSDonnees().get(i).getKey() != null || GestionDonneesCSV.getPosGPSDonnees().get(i).getValue() != null){
                    MapPoint pointTemp = new MapPoint(GestionDonneesCSV.getPosGPSDonnees().get(i).getKey(),
                            GestionDonneesCSV.getPosGPSDonnees().get(i).getValue());
                    pointMapDonnees.add(new Pair<>(pointTemp, GestionDonneesCSV.getIntensiteDonnees().get(i)));
                }
            }
        }
    }

    public static ObservableList<XYChart.Data<String, Number>> getDonneesScatterchart() {
        return DonneesScatterchart;
    }

    public static ObservableList<XYChart.Data<Number, Number>> getDonneesLineChart() {
        return DonneesLineChart;
    }

    public static ObservableList<Pair<MapPoint,Double>> getPointMapDonnees() {
        return pointMapDonnees;
    }
}
