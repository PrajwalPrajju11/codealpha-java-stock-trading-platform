# CodeAlpha_StockTradingPlatform

A console-based stock trading simulator built in Java as part of the CodeAlpha Java Programming Internship (Task 2).

## Features

- **Market data display:** view stock prices and daily % change
- **Buy / Sell:** trade shares with balance and ownership validation
- **Portfolio tracking:** holdings, average cost, profit/loss and overall return
- **Performance over time:** day-by-day portfolio value history
- **Transaction history:** log of every buy and sell
- **Persistence:** portfolio is saved to and loaded from `portfolio.txt` (File I/O)
- **OOP design:** `Stock`, `Market`, `User`, `Holding` and `Transaction` classes

## Project Structure

```
CodeAlpha_StockTradingPlatform/
├── StockTradingPlatform.java
└── README.md
```

## How to Run

Requires JDK 11 or higher.

```
javac StockTradingPlatform.java
java StockTradingPlatform
```

## Menu

| Option | Action |
|--------|--------|
| 1 | View market |
| 2 | Buy stock |
| 3 | Sell stock |
| 4 | My portfolio |
| 5 | Transaction history |
| 6 | Performance over time |
| 7 | Next day (update prices) |
| 8 | Save & exit |

## Sample Input

```
Ann
2
AAPL
10
7
4
8
```

## Notes

- Starting balance is $10,000.
- Prices move randomly by up to ±5% each simulated day.
- Delete `portfolio.txt` to start over with a fresh portfolio.
-
