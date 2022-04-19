package application;

import java.awt.MouseInfo;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class RandomGenerator implements Initializable {
	@FXML
	private Button startButton;

	@FXML
	private Button stopButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button saveButton;

	@FXML
	private Text leftTop;

	@FXML
	private Text vyber;

	@FXML
	private Text rightTop;

	@FXML
	private Text pocetBitu;

	ObservableList<Integer> pocB = FXCollections.observableArrayList();
	@FXML
	private ChoiceBox<Integer> vybranyPocetBitu;

	ObservableList<Integer> vybRych = FXCollections.observableArrayList();
	@FXML
	private ChoiceBox<Integer> vybranaRychlost;

	ObservableList<String> vyberVstupu = FXCollections.observableArrayList();
	@FXML
	private ChoiceBox<String> vybVstupu;

	@FXML
	private Text rychlostGenerovani;

	@FXML
	private TextArea t;

	long b;
	long a = System.currentTimeMillis();

	int resultA;
	int resultB;

	int pozadovanyPocet = 128;
	int pozadovanarychlostGenerovani = 32;

	@FXML
	private ProgressBar pBar;

	List<Byte> listOfBites = new ArrayList<Byte>();

	List<String> heslo = new ArrayList<String>();

	final Service<Void> thread = new Service<Void>() {
		// nove vlakno potrebne pro funkcnost ProgressBar aby PBar fungoval byla potreba
		// i funkci pro generovani umistit na stejne vlakno
		@Override
		public Task<Void> createTask() {
			return new Task<Void>() {

				@Override
				public Void call() throws InterruptedException {
					resultA = 2;
					resultB = 2;
					while (listOfBites.size() < pozadovanyPocet) {
						//System.out.println(listOfBites.size());
						// System.out.println(String.format("%d %d", resultA, resultB));
						generator(listOfBites);
						pBar.setProgress(listOfBites.size() / (double) pozadovanyPocet);
						try {
							TimeUnit.MILLISECONDS.sleep(1000 / pozadovanarychlostGenerovani);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch blocks
							return null;
						}

					}
					return null;
				}
			};
		}
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		zvolpocet();
		zvolRychlost();
		vyberVstup();
		pocetBitu.setText("Zvolte pozadovany pocet bitu:");
		vyber.setText("Vyber vstupu: ");
		rychlostGenerovani.setText("Zvolte rychlost nacitani polohy kurzoru:\n(pocet hodnot za sekundu)");
		rightTop.setFont(Font.font(null, FontWeight.BOLD, 16));
		leftTop.setFont(Font.font(null, FontWeight.BOLD, 16));
		stopButton.setDisable(true);
		saveButton.setDisable(true);
		deleteButton.setDisable(true);
		t.appendText(
				"Po stisknuti tlacitka \"START\" zacne generovani nahodnych bitu. Pro generovani je potreba pohybovat kurzorem po displeji. Po vygenerovani pozadovaneho poctu bitu budete upozorneni zvukem pipnuti.\n");

		vybranyPocetBitu.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				// TODO Auto-generated method stub
				pozadovanyPocet = vybranyPocetBitu.getSelectionModel().getSelectedItem();
				// t.appendText("Zvoleny pocet vygenerovanych bitu je " + pozadovanyPocet +
				// ".\n");

			}
		});

		vybranaRychlost.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				// TODO Auto-generated method stub
				pozadovanarychlostGenerovani = vybranaRychlost.getSelectionModel().getSelectedItem();
				// t.appendText("Rychlost generovani je nastavena na " +
				// pozadovanarychlostGenerovani + ".\n");
			}
		});

		thread.setOnSucceeded((e) -> {
			t.clear();
			stopButton.setDisable(true);
			saveButton.setDisable(false);
			deleteButton.setDisable(false);

			for (int j = 0; j < listOfBites.size(); j++) {
				if (listOfBites.get(j) == 0) {
					t.appendText("0");
				}
				t.appendText("1");
			}

			prevodNaHeslo(listOfBites);

			java.awt.Toolkit.getDefaultToolkit().beep();

			// t.appendText("Zvoleny pocet vygenerovanych bitu je " + pozadovanyPocet +
			// ".\n");
			// t.appendText("Rychlost generovani je nastavena na " +
			// pozadovanarychlostGenerovani + ".\n");

		});
	}

	public void positionStop(MouseEvent event) {
		startButton.setDisable(false);
		stopButton.setDisable(true);
		saveButton.setDisable(true);
		deleteButton.setDisable(true);
		t.clear();
		t.appendText("Generovani je vypnuto.\n");
		listOfBites.clear();
		heslo.clear();
		thread.cancel();
		pBar.setProgress(0);
		vybranyPocetBitu.setDisable(false);
		vybranaRychlost.setDisable(false);
	}

	public void positionStart(MouseEvent event) throws FileNotFoundException {
		b = System.currentTimeMillis();
		startButton.setDisable(true);
		stopButton.setDisable(false);
		vybranyPocetBitu.setDisable(true);
		vybranaRychlost.setDisable(true);

		thread.restart();

		t.clear();
		t.appendText("Zvoleny pozadovany pocet vygenerovanych bitu je " + pozadovanyPocet + ".\n");
		t.appendText("Rychlost generovani je nastavena na " + pozadovanarychlostGenerovani + " bitů/sekundu.\n");

	}

	public void generator(List<Byte> listOfBites) {
		int mouseX;
		int mouseY;
		int result;
		Byte finalResult = 0;

		mouseX = (int) MouseInfo.getPointerInfo().getLocation().getX(); // pozice kurzoru X
		mouseY = (int) MouseInfo.getPointerInfo().getLocation().getY(); // pozice kurzoru Y
		// System.out.println("X=" + mouseX + " ; Y=" + mouseY + "\n");
		// System.out.println((b - a) + "\n");

		if ((b - a) % 2 == 0) {
			b = System.currentTimeMillis();
		}

		if (mouseX > mouseY) {
			result = (int) (((mouseX - mouseY) * (b - a)) % (2));
		} else {
			result = (int) (((mouseY - mouseX) * (b - a)) % (2));
			if (result < 0) {
				result *= (-1);
			}
		}

		if (resultA == 2 && resultB == 2) {
			resultA = result;
		} else if (resultA != 2 && resultB == 2) {
			resultB = result;
		} else if (resultA != 2 && resultB != 2) {
			if (resultA == 1 && resultB == 0) {
				finalResult = 1;
				listOfBites.add(finalResult);
			} else if (resultA == 0 && resultB == 1) {
				finalResult = 0;
				listOfBites.add(finalResult);
			}
			resultA = 2;
			resultB = 2;
		}
	}

	public void prevodNaHeslo(List<Byte> listOfBites) {
		heslo.clear();
		int cyklus = 0;
		Byte aktualniBit = 2;
		while (cyklus < (pozadovanyPocet / 8)) {
			int konecneCislo = 0;
			int umisteniBituArray = cyklus * 8;
			for (int j = 0; j < 8; j++) {
				aktualniBit = listOfBites.get(umisteniBituArray);
				// prevod 8bitu na jedno cislo
				if (aktualniBit == 0) {
					umisteniBituArray++;
				} else if (aktualniBit == 1) {
					konecneCislo += 1 * Math.pow(2, j);
					umisteniBituArray++;
				}
			}
			cyklus++;
			// vsechny mozne znaky
			List<String> znakyNaHeslo = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i",
					"j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2",
					"3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
					"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "@", "&"));
			heslo.add(znakyNaHeslo.get(konecneCislo % 64));
		}
		t.appendText("\n\nVygenerovane heslo: ");
		for (String znak : heslo) {
			t.appendText(znak);
		}
		t.appendText("\n");
	}

	public void soubor(MouseEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Ulozit soubor");

		FileChooser.ExtensionFilter filtr = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		chooser.getExtensionFilters().add(filtr);

		File zvolenySoubor = null;
		zvolenySoubor = chooser.showSaveDialog(null);

		if (zvolenySoubor == null) {
			t.appendText("Soubor pro ulozeni nebyl zvolen.");
			return;
		}

		File soubor = new File(zvolenySoubor.getAbsolutePath());

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(soubor);
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// vypis bitu do souboru
		for (Byte bite : listOfBites) {
			if (bite == 0) {
				writer.print("0");
			} else
				writer.print("1");
		}
		writer.println();
		// vypis hesla do soboru

		writer.print("Heslo: ");
		for (String znak : heslo) {
			writer.print(znak);
		}

		writer.close();
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	public void delete(MouseEvent event) {
		t.clear();
		listOfBites.clear();
		t.appendText("Vysledna sekvence je smazana.\n");
		saveButton.setDisable(true);
		deleteButton.setDisable(true);
		startButton.setDisable(false);
		pBar.setProgress(0);
		vybranyPocetBitu.setDisable(false);
		vybranaRychlost.setDisable(false);
	}

	private void zvolpocet() {
		Integer a = 16;
		Integer b = 32;
		Integer c = 64;
		Integer d = 128;
		Integer e = 256;
		Integer f = 512;
		Integer g = 1024;
		Integer h = 2048;
		Integer k = 10000;
		Integer i = 50000;
		Integer j = 1000000;
		pocB.addAll(a, b, c, d, e, f, g, h, k, i, j);
		vybranyPocetBitu.getItems().addAll(pocB);
		vybranyPocetBitu.getSelectionModel().select(d);
	}

	private void zvolRychlost() {
		Integer a = 1;
		Integer b = 2;
		Integer c = 4;
		Integer d = 8;
		Integer e = 16;
		Integer f = 32;
		Integer g = 64;
		Integer h = 128;
		Integer i = 256;
		vybRych.addAll(a, b, c, d, e, f, g, h, i);
		vybranaRychlost.getItems().addAll(vybRych);
		vybranaRychlost.getSelectionModel().select(f);
	}

	private void vyberVstup() {
		String mys = "mys";
		vyberVstupu.addAll(mys);
		vybVstupu.getItems().addAll(vyberVstupu);
		vybVstupu.getSelectionModel().select(mys);
	}
}
