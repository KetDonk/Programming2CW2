package blackjack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * This class implements a simple version of the Blackjack game using Swing for the GUI.
 */
public class Main extends JFrame {
    // GUI components
    private JLabel dealerLabel;
    private JLabel playerLabel;
    private JLabel dealerTotalLabel;
    private JLabel playerTotalLabel;
    private JButton hitButton;
    private JButton standButton;
    private JLabel betLabel;
    private JLabel moneyLabel;

    // Game variables
    private ArrayList<Card> deck;
    private ArrayList<Card> dealerHand;
    private ArrayList<Card> playerHand;

    private int playerMoney = 1000;
    private int currentBet = 5;
    private final double reshuffleThreshold = 3.5 * 52 * 8; // 3.5 decks used before reshuffle
    private int cardsDealt = 0;

    /**
     * Constructor for the Main class. Sets up the GUI components and initializes the game.
     */
    public Main() {
        setTitle("Blackjack Game");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create panels for dealer and player hands
        JPanel dealerPanel = createHandPanel("Dealer's hand");
        JPanel playerPanel = createHandPanel("Player's hand");

        // Create top panel containing dealer and player panels
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(dealerPanel);
        topPanel.add(playerPanel);

        // Create button panel containing Hit, Stand, Bet, and Money labels
        JPanel buttonPanel = new JPanel(new FlowLayout());
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        betLabel = new JLabel("Bet: £" + currentBet);
        moneyLabel = new JLabel("Money: £" + playerMoney);

        // Set button colors
        hitButton.setBackground(Color.GREEN);
        standButton.setBackground(Color.RED);

        // Add components to button panel
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(betLabel);
        buttonPanel.add(moneyLabel);

        // Add panels to the frame
        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to buttons
        hitButton.addActionListener(e -> hit());
        standButton.addActionListener(e -> stand());

        // Initialize deck and start the game
        initializeDeck();
        startGame();
    }

    /**
     * Creates a panel to display a hand with the given title.
     * @param title The title for the hand panel.
     * @return A JPanel instance representing the hand panel.
     */
    private JPanel createHandPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add margins
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), panel.getBorder()));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        JLabel cardsLabel = new JLabel("", SwingConstants.CENTER);
        panel.add(cardsLabel, BorderLayout.CENTER);
        JLabel totalLabel = new JLabel("Total: ", SwingConstants.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        if (title.equals("Dealer's hand")) {
            dealerLabel = cardsLabel;
            dealerTotalLabel = totalLabel;
        } else {
            playerLabel = cardsLabel;
            playerTotalLabel = totalLabel;
        }
        return panel;
    }

    /**
     * Initializes the deck with 8 sets of standard playing cards and shuffles it.
     */
    private void initializeDeck() {
        deck = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    deck.add(new Card(rank, suit));
                }
            }
        }
        Collections.shuffle(deck);
    }

    /**
     * Starts a new round of the game by initializing hands and dealing cards.
     */
    private void startGame() {
        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        if (deck.size() < 4) {
            shuffleDeck();
        }
        dealCard(dealerHand);
        dealCard(playerHand);
        dealCard(playerHand);
        updateHands();
    }

    /**
     * Deals a card to the specified hand, shuffling the deck if necessary.
     * @param hand The hand to deal the card to.
     */
    private void dealCard(ArrayList<Card> hand) {
        if (cardsDealt >= reshuffleThreshold) {
            shuffleDeck();
            JOptionPane.showMessageDialog(this, "Deck reshuffled");
            cardsDealt = 0;
        }
        Card card = deck.remove(0);
        hand.add(card);
        cardsDealt++;
    }

    /**
     * Shuffles the deck.
     */
    private void shuffleDeck() {
        initializeDeck();
    }

    /**
     * Updates the GUI to reflect the current hands and their totals.
     */
    private void updateHands() {
        // Update dealer's hand
        StringBuilder dealerHandText = new StringBuilder();
        for (Card card : dealerHand) {
            dealerHandText.append(card.toString()).append("<br>");
        }
        dealerLabel.setText("<html>" + dealerHandText.toString() + "</html>");
        dealerTotalLabel.setText("Total: " + calculateHandValue(dealerHand));

        // Update player's hand
        StringBuilder playerHandText = new StringBuilder();
        for (Card card : playerHand) {
            playerHandText.append(card.toString()).append("<br>");
        }
        playerLabel.setText("<html>" + playerHandText.toString() + "</html>");
        playerTotalLabel.setText("Total: " + calculateHandValue(playerHand));
    }

    /**
     * Handles the Hit action by dealing a card to the player's hand and checking for bust.
     */
    private void hit() {
        dealCard(playerHand);
        int playerValue = calculateHandValue(playerHand);
        if (playerValue > 21) {
            JOptionPane.showMessageDialog(this, "You are bust! You lose £" + currentBet);
            playerMoney -= currentBet;
            moneyLabel.setText("Money: £" + playerMoney);
            startGame();
        } else {
            updateHands();
        }
    }

    /**
     * Handles the Stand action by dealing cards to the dealer's hand and determining the winner.
     */
    private void stand() {
        if (dealerHand.size() == 2) {
            updateHands();
        }
        int dealerValue = calculateHandValue(dealerHand);
        while (dealerValue < 17) {
            dealCard(dealerHand);
            dealerValue = calculateHandValue(dealerHand);
        }
        int playerValue = calculateHandValue(playerHand);
        StringBuilder message = new StringBuilder();
        message.append("Dealer's Total: ").append(dealerValue).append("\n");
        message.append("Dealer's Hand: ");
        for (Card card : dealerHand) {
            message.append(card.toString()).append(", ");
        }
        message.deleteCharAt(message.length() - 2);

        if (dealerValue > 21 || (dealerValue < playerValue && playerValue <= 21)) {
            message.append("\n\nYou win £").append(currentBet * 2);
            playerMoney += currentBet * 2;
        } else if (playerValue == dealerValue) {
            message.append("\n\nIt's a draw. You get back your bet of £").append(currentBet);
            playerMoney += currentBet;
        } else {
            message.append("\n\nYou lose £").append(currentBet);
            playerMoney -= currentBet; // Deduct the current bet from player's money
        }
        moneyLabel.setText("Money: £" + playerMoney); // Update money label

        JOptionPane.showMessageDialog(this, message.toString());

        startGame();
    }

    /**
     * Calculates the value of a hand, considering the special case of an Ace.
     * @param hand The hand to calculate the value for.
     * @return The value of the hand.
     */
    private int calculateHandValue(ArrayList<Card> hand) {
        int value = 0;
        boolean hasAce = false;
        for (Card card : hand) {
            value += card.getValue();
            if (card.getRank() == Rank.ACE) {
                hasAce = true;
            }
        }
        if (hasAce && value + 10 <= 21) {
            value += 10;
        }
        return value;
    }

    /**
     * Main method to start the application.
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}

/**
 * Enum representing the suits of playing cards.
 */
enum Suit {
    SPADES, HEARTS, DIAMONDS, CLUBS
}

/**
 * Enum representing the ranks of playing cards along with their values.
 */
enum Rank {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10), ACE(11);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /**
     * Gets the value associated with the rank.
     * @return The value of the rank.
     */
    public int getValue() {
        return value;
    }
}

/**
 * Class representing a playing card.
 */
class Card {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructor for creating a card with the given rank and suit.
     * @param rank The rank of the card.
     * @param suit The suit of the card.
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Gets the rank of the card.
     * @return The rank of the card.
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gets the suit of the card.
     * @return The suit of the card.
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Gets the value of the card.
     * @return The value of the card.
     */
    public int getValue() {
        return rank.getValue();
    }

    /**
     * Returns a string representation of the card.
     * @return A string representing the card.
     */
    public String toString() {
        return rank + " of " + suit;
    }
}