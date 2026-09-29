import java.io.*;
import java.util.*;

/**
 * Task 2: Stock Trading Platform (console-based)
 * Features: market data display, buy/sell, portfolio performance tracking,
 * OOP design, and file I/O persistence (portfolio.txt).
 */
public class StockTradingPlatform {

    // ---------- Stock ----------
    static class Stock {
        private final String symbol, name;
        private double price, prevPrice;

        Stock(String symbol, String name, double price) {
            this.symbol = symbol; this.name = name;
            this.price = price; this.prevPrice = price;
        }
        String getSymbol() { return symbol; }
        String getName() { return name; }
        double getPrice() { return price; }
        double getChangePct() { return (price - prevPrice) / prevPrice * 100; }

        void fluctuate(Random rnd) {
            prevPrice = price;
            double change = (rnd.nextDouble() - 0.5) * 0.10; // -5% to +5%
            price = Math.max(1.0, Math.round(price * (1 + change) * 100) / 100.0);
        }
    }

    // ---------- Market ----------
    static class Market {
        private final Map<String, Stock> stocks = new LinkedHashMap<>();
        private final Random rnd = new Random();

        Market() {
            add(new Stock("AAPL", "Apple Inc.", 175.00));
            add(new Stock("GOOG", "Alphabet Inc.", 140.50));
            add(new Stock("MSFT", "Microsoft Corp.", 330.25));
            add(new Stock("AMZN", "Amazon.com Inc.", 125.80));
            add(new Stock("TSLA", "Tesla Inc.", 250.10));
            add(new Stock("INFY", "Infosys Ltd.", 18.40));
        }
        private void add(Stock s) { stocks.put(s.getSymbol(), s); }
        Stock get(String symbol) { return stocks.get(symbol.toUpperCase()); }
        void nextDay() { stocks.values().forEach(s -> s.fluctuate(rnd)); }

        void display() {
            System.out.println("\n=========== MARKET DATA ===========");
            System.out.printf("%-6s %-18s %10s %9s%n", "SYMBOL", "COMPANY", "PRICE($)", "CHANGE");
            for (Stock s : stocks.values()) {
                System.out.printf("%-6s %-18s %10.2f %+8.2f%%%n",
                        s.getSymbol(), s.getName(), s.getPrice(), s.getChangePct());
            }
        }
    }

    // ---------- Holding ----------
    static class Holding {
        final String symbol;
        int quantity;
        double avgCost;
        Holding(String symbol, int quantity, double avgCost) {
            this.symbol = symbol; this.quantity = quantity; this.avgCost = avgCost;
        }
    }

    // ---------- Transaction ----------
    static class Transaction {
        final String type, symbol;
        final int quantity, day;
        final double price;
        Transaction(String type, String symbol, int quantity, double price, int day) {
            this.type = type; this.symbol = symbol;
            this.quantity = quantity; this.price = price; this.day = day;
        }
        @Override public String toString() {
            return String.format("Day %-3d %-4s %-6s x%-4d @ $%.2f  = $%.2f",
                    day, type, symbol, quantity, price, quantity * price);
        }
    }

    // ---------- User / Portfolio ----------
    static class User {
        static final double START_CASH = 10000.00;
        private final String name;
        private double cash = START_CASH;
        private int day = 1;
        private final Map<String, Holding> holdings = new LinkedHashMap<>();
        private final List<Transaction> transactions = new ArrayList<>();
        private final List<double[]> valueHistory = new ArrayList<>(); // {day, value}

        User(String name) { this.name = name; }
        String getName() { return name; }
        double getCash() { return cash; }
        int getDay() { return day; }
        void advanceDay() { day++; }

        boolean buy(Stock s, int qty) {
            double cost = s.getPrice() * qty;
            if (qty <= 0) { System.out.println("Quantity must be positive."); return false; }
            if (cost > cash) {
                System.out.printf("Insufficient funds. Need $%.2f, have $%.2f%n", cost, cash);
                return false;
            }
            cash -= cost;
            Holding h = holdings.get(s.getSymbol());
            if (h == null) {
                holdings.put(s.getSymbol(), new Holding(s.getSymbol(), qty, s.getPrice()));
            } else {
                h.avgCost = (h.avgCost * h.quantity + cost) / (h.quantity + qty);
                h.quantity += qty;
            }
            transactions.add(new Transaction("BUY", s.getSymbol(), qty, s.getPrice(), day));
            return true;
        }

        boolean sell(Stock s, int qty) {
            Holding h = holdings.get(s.getSymbol());
            if (qty <= 0) { System.out.println("Quantity must be positive."); return false; }
            if (h == null || h.quantity < qty) {
                System.out.println("You don't own enough shares of " + s.getSymbol());
                return false;
            }
            cash += s.getPrice() * qty;
            h.quantity -= qty;
            if (h.quantity == 0) holdings.remove(s.getSymbol());
            transactions.add(new Transaction("SELL", s.getSymbol(), qty, s.getPrice(), day));
            return true;
        }

        double holdingsValue(Market m) {
            double total = 0;
            for (Holding h : holdings.values()) total += h.quantity * m.get(h.symbol).getPrice();
            return total;
        }
        double totalValue(Market m) { return cash + holdingsValue(m); }

        void recordSnapshot(Market m) {
            valueHistory.removeIf(v -> (int) v[0] == day);
            valueHistory.add(new double[]{day, totalValue(m)});
        }

        void showPortfolio(Market m) {
            System.out.println("\n=========== PORTFOLIO (" + name + ") ===========");
            if (holdings.isEmpty()) System.out.println("No holdings yet.");
            else {
                System.out.printf("%-6s %5s %10s %10s %11s%n", "SYMBOL", "QTY", "AVG COST", "PRICE", "P/L ($)");
                for (Holding h : holdings.values()) {
                    double price = m.get(h.symbol).getPrice();
                    System.out.printf("%-6s %5d %10.2f %10.2f %+11.2f%n",
                            h.symbol, h.quantity, h.avgCost, price, (price - h.avgCost) * h.quantity);
                }
            }
            double total = totalValue(m);
            System.out.printf("%nCash:            $%.2f%n", cash);
            System.out.printf("Holdings value:  $%.2f%n", holdingsValue(m));
            System.out.printf("Total value:     $%.2f%n", total);
            System.out.printf("Overall return:  %+.2f%%%n", (total - START_CASH) / START_CASH * 100);
        }

        void showTransactions() {
            System.out.println("\n=========== TRANSACTION HISTORY ===========");
            if (transactions.isEmpty()) System.out.println("No transactions yet.");
            transactions.forEach(System.out::println);
        }

        void showPerformance() {
            System.out.println("\n=========== PORTFOLIO PERFORMANCE ===========");
            if (valueHistory.isEmpty()) { System.out.println("No data yet."); return; }
            double prev = START_CASH;
            for (double[] v : valueHistory) {
                int bars = (int) Math.max(0, Math.min(40, v[1] / START_CASH * 20));
                System.out.printf("Day %-3d $%10.2f (%+.2f%%) %s%n", (int) v[0], v[1],
                        (v[1] - prev) / prev * 100, "#".repeat(bars));
                prev = v[1];
            }
        }

        // ---- File I/O ----
        void save(String file) {
            try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                out.println("USER," + name);
                out.println("CASH," + cash);
                out.println("DAY," + day);
                for (Holding h : holdings.values())
                    out.println("H," + h.symbol + "," + h.quantity + "," + h.avgCost);
                for (Transaction t : transactions)
                    out.println("T," + t.type + "," + t.symbol + "," + t.quantity + "," + t.price + "," + t.day);
                for (double[] v : valueHistory)
                    out.println("V," + (int) v[0] + "," + v[1]);
                System.out.println("Portfolio saved to " + file);
            } catch (IOException e) {
                System.out.println("Could not save: " + e.getMessage());
            }
        }

        static User load(String file) {
            File f = new File(file);
            if (!f.exists()) return null;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                User u = new User("Trader");
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    switch (p[0]) {
                        case "USER": u = new User(p[1]); break;
                        case "CASH": u.cash = Double.parseDouble(p[1]); break;
                        case "DAY": u.day = Integer.parseInt(p[1]); break;
                        case "H": u.holdings.put(p[1], new Holding(p[1],
                                Integer.parseInt(p[2]), Double.parseDouble(p[3]))); break;
                        case "T": u.transactions.add(new Transaction(p[1], p[2],
                                Integer.parseInt(p[3]), Double.parseDouble(p[4]), Integer.parseInt(p[5]))); break;
                        case "V": u.valueHistory.add(new double[]{
                                Integer.parseInt(p[1]), Double.parseDouble(p[2])}); break;
                    }
                }
                return u;
            } catch (Exception e) {
                System.out.println("Save file unreadable, starting fresh.");
                return null;
            }
        }
    }

    // ---------- Main ----------
    static final String SAVE_FILE = "portfolio.txt";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Market market = new Market();
        User user = User.load(SAVE_FILE);

        if (user == null) {
            System.out.print("Welcome! Enter your name: ");
            String n = sc.nextLine().trim();
            user = new User(n.isEmpty() ? "Trader" : n);
        } else {
            System.out.println("Welcome back, " + user.getName() + "! Portfolio loaded (Day " + user.getDay() + ").");
        }
        user.recordSnapshot(market);

        boolean running = true;
        while (running) {
            System.out.println("\n--- STOCK TRADING PLATFORM ---");
            System.out.printf("Day %d | Cash: $%.2f | Total: $%.2f%n",
                    user.getDay(), user.getCash(), user.totalValue(market));
            System.out.println("1. View market      5. Transactions");
            System.out.println("2. Buy stock        6. Performance over time");
            System.out.println("3. Sell stock       7. Next day (update prices)");
            System.out.println("4. My portfolio     8. Save & exit");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1": market.display(); break;
                case "2": trade(sc, market, user, true); break;
                case "3": trade(sc, market, user, false); break;
                case "4": user.showPortfolio(market); break;
                case "5": user.showTransactions(); break;
                case "6": user.recordSnapshot(market); user.showPerformance(); break;
                case "7":
                    user.advanceDay(); market.nextDay();
                    user.recordSnapshot(market);
                    System.out.println("Market updated for Day " + user.getDay() + ".");
                    market.display();
                    break;
                case "8": user.save(SAVE_FILE); running = false; break;
                default: System.out.println("Invalid option.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void trade(Scanner sc, Market market, User user, boolean buying) {
        market.display();
        System.out.print("Enter symbol: ");
        Stock s = market.get(sc.nextLine().trim());
        if (s == null) { System.out.println("Unknown symbol."); return; }
        System.out.print("Quantity: ");
        try {
            int qty = Integer.parseInt(sc.nextLine().trim());
            boolean ok = buying ? user.buy(s, qty) : user.sell(s, qty);
            if (ok) {
                System.out.printf("%s %d x %s @ $%.2f successful.%n",
                        buying ? "Bought" : "Sold", qty, s.getSymbol(), s.getPrice());
                user.recordSnapshot(market);
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid whole number.");
        }
    }
}