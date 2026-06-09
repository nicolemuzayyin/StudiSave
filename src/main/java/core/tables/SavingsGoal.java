package core.tables;

// authors: Nicole Muzayyin, Felix D'Cruz

import core.Session;
import core.db_functions.CalcField;

public class SavingsGoal {

    private String savingID;
    private String userID;
    private String name;
    private double amount;
    private String startDate;
    private String endDate;
    private String note;
    private double perMonth;
    private int completed;
    private double amountSaved;

    // used when loading an existing savings goal from the database
    public void fromRow(String savingID, String userID, String name, double amount,
                        String startDate, String endDate, String note, double perMonth, int completed, double amountSaved) {
        this.savingID    = savingID;
        this.userID      = userID;
        this.name        = name;
        this.amount      = amount;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.note        = note;
        this.perMonth    = perMonth;
        this.completed   = completed;
        this.amountSaved = amountSaved;
    }

    // get data from validated user input, generate non-user fields
    public void fromUserInput(String name, double amount,
                              String startDate, String endDate, String note) {
        this.savingID    = CalcField.newUUID();
        this.userID      = Session.getUser().getUserID();
        this.name        = (name == null || name.isBlank())        ? "Unnamed Goal" : name;
        this.amount      = amount;
        this.startDate   = (startDate == null || startDate.isBlank()) ? CalcField.nowDate() : startDate;
        this.endDate     = (endDate   == null || endDate.isBlank())   ? CalcField.nowDate() : endDate;
        this.note        = (note != null) ? note : "";
        this.perMonth    = CalcField.calcPerMonth(amount, this.startDate, this.endDate);
        this.completed   = 0;
        this.amountSaved = 0;
        System.out.println("TABLE: New SavingsGoal created: " + this.name);
    }

    public String getSavingID()    { return savingID;    }
    public String getUserID()      { return userID;      }
    public String getName()        { return name;        }
    public double getAmount()      { return amount;      }
    public String getStartDate()   { return startDate;   }
    public String getEndDate()     { return endDate;     }
    public String getNote()        { return note;        }
    public double getPerMonth()    { return perMonth;    }
    public int    getCompleted()   { return completed;   }
    public double getAmountSaved()  { return amountSaved; }

    public void setName(String name)           { this.name = name;        }
    public void setNote(String note)           { this.note = note;        }
    public void setCompleted(int completed)    { this.completed = completed; }
    public void setPerMonth(double perMonth)   { this.perMonth = perMonth;   }
}
