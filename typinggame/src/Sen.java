
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * @author Magdalena Gerszewska
 */

public class Sen extends JFrame {

    private int liczbaZyc = 3;

    JButton b = null;
    JPanel p = null;
    JTextField tf = null;
    JLabel label = null;
    int score = 0;
    ArrayList<String> wordlist;
    WordRun word1 = null;
    WordRun word2 = null;
    WordRun word3 = null;
    
    /**
     * Dźwięki wykorzystane w grze
     */
    private Sound przegranaDzwiek = new Sound("Dzwieki/gameover.wav");
    private Sound punktDzwiek = new Sound("Dzwieki/point.wav");
    private Sound bladDzwiek = new Sound("Dzwieki/error.wav");
//  Background music:
//  April showers by Kjartan Abel.
//  Visit https://kjartan-abel.com/library to download royalty-free music for your next project.
//  CC BY-SA 4.0 Attribution-ShareAlike 4.0 International.
    private Sound bgDzwiek = new Sound("Dzwieki/back.wav");

    boolean przegrana = false;
    
    /**
     * Klasa do odtwarzania dźwięków
     */
    public class Sound {

        AudioInputStream sound;
        Clip clip;

        public Sound(String fileName) {
            try {
                sound = AudioSystem.getAudioInputStream(new File(fileName));
                clip = AudioSystem.getClip();
                clip.stop();
                clip.open(sound);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void playPoint() {
            clip.setFramePosition(0);
            clip.start();
        }

        public void playOver() {
            clip.start();
        }

        public void stop() {
            clip.stop();
        }
    }

    /**
     * Główny konstruktor klasy
     */
    public Sen() {
        super("Sen króla Sejonga");
        setSize(1280, 800);
        setLayout(new BorderLayout());
        bgDzwiek.playOver();

        b = new JButton("Start");
        b.addActionListener(new ButtonHandler());
        add(b, BorderLayout.NORTH);

        p = new DrawPanel();
        p.setPreferredSize(new Dimension(300, 300));
        add(p, BorderLayout.CENTER);

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());

        tf = new JTextField(10);
        tf.getDocument().addDocumentListener(new ListenText());
        p2.add(tf);

        label = new JLabel("0");
        p2.add(label);

        add(p2, BorderLayout.SOUTH);
        
        
        /**
        * Wczytanie listy słów z pliku tekstowego
        */

        FileInputStream textFile;
        BufferedReader readWords;

        wordlist = new ArrayList<>();

        try {
            textFile = new FileInputStream("level 3.txt");
            readWords = new BufferedReader(new InputStreamReader(textFile));

            String line = readWords.readLine();
            while (line != null) {
                wordlist.add(line);
                line = readWords.readLine();
            }
            textFile.close();
        } catch (IOException e) {

        }

        word1 = new WordRun();
        word2 = new WordRun();
        word3 = new WordRun();

        word1.st = null;
        word2.st = null;
        word3.st = null;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        show();
    }

    /**
     * Utwórz interfejs graficzny użytkownika
     */
    class DrawPanel extends JPanel {

        public void paintComponent(Graphics g) {

            try {
                BufferedImage bg = ImageIO.read(new File("Obrazy/BackgroundImg.jpg"));
                BufferedImage life = ImageIO.read(new File("Obrazy/life2.png"));
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                Font f = new Font("Batang", Font.PLAIN, 50);
                FontMetrics fm = this.getFontMetrics(f);
                g2.setColor(Color.BLACK);
                g2.setFont(f);
                if (word1.st != null) {
                    g2.drawString(word1.st, word1.posx, word1.posy);
                }
                if (word2.st != null) {
                    g2.drawString(word2.st, word2.posx, word2.posy);
                }
                if (word3.st != null) {
                    g2.drawString(word3.st, word3.posx, word3.posy);
                }

                for (int i = 0; i < liczbaZyc; i++) {
                    g2.drawImage(life, 25 + i * 65, 20, this);
                    repaint();
                }
            } catch (IOException ex) {
                Logger.getLogger(Sen.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (word1.posy > 700) {
                liczbaZyc--;
                bladDzwiek.playPoint();
                word1.posy = 0;
            }
            if (word2.posy > 700) {
                liczbaZyc--;
                bladDzwiek.playPoint();
                word2.posy = 0;
            }
            if (word3.posy > 700) {
                liczbaZyc--;
                bladDzwiek.playPoint();
                word3.posy = 0;
            }
            if (przegrana) {
                g.setColor(Color.red);
                g.setFont(new Font("Gill Sans MT", Font.BOLD, 80));
                g.drawString(String.format("KONIEC GRY! WYNIK: " + Integer.toString(score)), 120, (700 - 120) / 2);
                g.setColor(Color.black);
                przegranaDzwiek.playOver();

                word1.st = null;
                word2.st = null;
                word3.st = null;
            }
            if (liczbaZyc <= 0) {
                przegrana = true;
                repaint();
                return;
            }
        }
    }

    /**
     * Klasa wywołująca słowa na ekranie
     */
    
    class WordRun implements Runnable {

        String st = null;
        int posx;
        int posy;

        public void run() {

            try {
                Random rand = null;
                while (true) {
                    if ((st == null) || (posy == p.getHeight())) {
                        rand = new Random();
                        Thread.currentThread().sleep(rand.nextInt(1000));
                        st = wordlist.get(rand.nextInt(56));
                        posy = 0;
                        do {
                            posx = rand.nextInt(p.getWidth());
                        } while (posx > (p.getWidth() - 100));
                        tf.setText("");
                    } else {
                        Thread.currentThread().sleep(20);
                        posy++;
                    }
                    repaint();
                }

            } catch (Exception e) {
            }

        }

    }
    
    /**
     * Klasa odpowiadająca naciśnięciu przycisku start
     */

    class ButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand() == "Start") {
                Thread t1 = new Thread(word1);
                Thread t2 = new Thread(word2);
                Thread t3 = new Thread(word3);
                t1.start();
                t2.start();
                t3.start();
            }

        }
    }
    
    /**
     * Klasa sprawdzająca, czy słowo zostało poprawnie wpisane
     */

    class ListenText implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
        }

        public void insertUpdate(DocumentEvent e) {
            if (tf.getText().equals(word1.st)) {
                word1.st = null;
                word1.posy = 0;
                score++;
                punktDzwiek.playPoint();
                label.setText(Integer.toString(score));
            }
            if (tf.getText().equals(word2.st)) {
                word2.st = null;
                word2.posy = 0;
                score++;
                punktDzwiek.playPoint();
                label.setText(Integer.toString(score));
            }
            if (tf.getText().equals(word3.st)) {
                word3.st = null;
                word3.posy = 0;
                score++;
                punktDzwiek.playPoint();
                label.setText(Integer.toString(score));
            }
        }
    }
    
    /**
     * Uruchomienie gry
     */

    public static void main(String[] args) {
        Sen ap = new Sen();
    }
}
