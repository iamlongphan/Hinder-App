/**
 *  Version: 1.0
 *  Date: 12/5/2023
 *  Code written by: Long Phan
 *
 */

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;
import java.util.Properties;
import java.io.FileInputStream;
class hinderApp
{
    public static void main(String[] args) throws IOException {

        //Properties HERE FOR GRADING
        FileReader reader = new FileReader("src/app.properties");
        Properties appProps = new Properties();
        appProps.load(reader);

        String databaseName = appProps.getProperty("database");
        String username = appProps.getProperty("user");
        String password = appProps.getProperty("password");
        String connection = appProps.getProperty("connection");
        //End Properties loading

        //Vars
        String signInName = "";
        String signInPass = "";

        //End
        boolean exit = false;
        Connection conn = null;
        ResultSet rs = null;
        ResultSet result2 = null;
        Statement stmt = null;
        int msgId = 0;
        while(!exit) {
            try {
                LocalDate currentDate = LocalDate.now();
                System.out.println(currentDate);
                conn = DriverManager.getConnection(
                        "jdbc:mysql://"+ connection + "/" + databaseName + "?" +
                                "user=" + username + "&" + "password=" + password);
                System.out.println("Welcome to the Hinder app, hating is appreciated, love is depreciated!");
                System.out.println("The most recently added hinder recipients are: ");
                System.out.println(" ");
                stmt = conn.createStatement();
                //A prepared statement for sending to members
                Random rand = new Random();
                int randomInt = rand.nextInt();
                //Finding certain names
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Member WHERE Name =?");
                //Priv msg creation
                PreparedStatement sendMsg = conn.prepareStatement("INSERT INTO PrivateMessage VALUES(?,?, "+ randomInt + ", ?, ?)");
                //posting a comment creation
                PreparedStatement postCom = conn.prepareStatement("INSERT INTO Comment VALUES (?,?,?," + randomInt +",?)");
                PreparedStatement newEmail = conn.prepareStatement("UPDATE Member SET Email = ? WHERE Member.name = ?");
                PreparedStatement newPassword = conn.prepareStatement("UPDATE Member SET password = ? WHERE Member.name = ?");
                PreparedStatement newComment = conn.prepareStatement("UPDATE Comment SET Content = ? WHERE Comment.commentID = ?");
                PreparedStatement deleteVictim = conn.prepareStatement("DELETE FROM Victim WHERE Victim.SSN = ?");
                PreparedStatement deleteAccount = conn.prepareStatement("DELETE FROM Member WHERE Member.Name = ?");
                PreparedStatement viewVictims = conn.prepareStatement("SELECT * FROM commentSSN");
                PreparedStatement registration = conn.prepareStatement("INSERT INTO Member VALUES (?,?,?)");
                rs = stmt.executeQuery("SELECT * FROM recentFive ORDER BY victimID DESC");
                int highestID = 0;
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(count + ". " + rs.getString("firstName") + " " + rs.getString("lastName"));
                    if(count == 5)
                    {
                        break;
                    }
                    if(count == 1)
                    {
                       highestID = rs.getInt("victimID");
                    }
                }
                //New victim statement
                PreparedStatement newVic = conn.prepareStatement("INSERT INTO Victim VALUES (?,?,?,?,?,?,"+ (highestID + 1) + ")");
                System.out.println(" ");
                System.out.println("1. Sign In");
                System.out.println("2. Register");
                System.out.println("3. Exit Application");
                System.out.println("Choose an option from the above: ");
                Scanner scan = new Scanner(System.in);
                int readInput = scan.nextInt();

                switch (readInput) {
                    case 1:
                        System.out.println("Please enter username: ");
                        signInName = scan.next();
                        System.out.println("Please enter password: ");
                        signInPass = scan.next();
                        rs = stmt.executeQuery("SELECT Name, password FROM Member WHERE name = '" + signInName + "' AND password = '" + signInPass + "'");
                        if (!rs.next()) {
                            System.out.println("Sorry that user does not exist!");
                            break;
                        }
                        else {
                            while(!exit)
                            {
                                int option = 0;
                                result2 = stmt.executeQuery("SELECT Email FROM Member WHERE name = '" + signInName + "'");
                                String currentEmail = "";
                                while(result2.next()) {
                                    currentEmail = result2.getString("Email");
                                }
                                //System.out.println(currentEmail);
                                System.out.println("Welcome in " + signInName);
                                System.out.println(" ");
                                System.out.println("1. Send a private message ");
                                System.out.println("2. Post a comment ");
                                System.out.println("3. Post a new victim ");
                                System.out.println("4. Change Email ");
                                System.out.println("5. Change Password ");
                                System.out.println("6. Edit a comment ");
                                System.out.println("7. Remove a hinder victim");
                                System.out.println("8. Delete your account");
                                System.out.println("9. View current victims");
                                System.out.println("10. Exit app");
                                System.out.println(" ");
                                System.out.println("What would you like to do (Type a number of one of the options above): ");
                                option = scan.nextInt();

                                switch (option) {
                                    case 1:
                                        System.out.println("Please name someone to send a message to: ");
                                        String receiver = scan.next();
                                        rs = stmt.executeQuery("SELECT Name, password FROM Member WHERE name = '" + receiver + "'");
                                        if(!rs.next())
                                        {
                                            System.out.println("User doesn't exist");
                                            System.out.println(" ");
                                            break;
                                        }
                                        else {
                                            ps.setString(1, receiver);
                                            result2 = ps.executeQuery();
                                            if (result2.next()) {
                                                System.out.println("What is your message?");
                                                String msg = scan.next();
                                                sendMsg.setString(1, String.valueOf(currentDate));
                                                sendMsg.setString(2, msg);
                                                sendMsg.setString(3, currentEmail);
                                                sendMsg.setString(4, receiver);
                                                sendMsg.executeUpdate();
                                                System.out.println("Message sent.");
                                            }
                                        }
                                        break;
                                        //STORE PROCEDURE UNDER CASE 2 ON LINE rs =
                                    case 2:
                                        rs = stmt.executeQuery("CALL getAllVictims");
                                        System.out.println("Victims for Comment");
                                        while(rs.next())
                                        {
                                            System.out.println("First Name: " + rs.getString("firstName") + " Last Name: " + rs.getString("lastName") + " SSN: " + rs.getString("SSN"));
                                        }
                                        System.out.println("Please type the SSN of the victim you wish to comment on");
                                        String vicSSN = scan.next();
                                        String date = String.valueOf(currentDate);
                                        String currentUser = signInName;
                                        System.out.println("Please type in comment content: ");
                                        scan.nextLine();
                                        String wholeComment = scan.nextLine();
                                        postCom.setString(1, date);
                                        postCom.setString(2, wholeComment);
                                        postCom.setString(3, currentUser);
                                        postCom.setString(4, vicSSN);
                                        postCom.executeUpdate();
                                        System.out.println("Comment posted");
                                        break;
                                    case 3:
                                        System.out.println("Please describe the victim: ");
                                        String desc = scan.next();
                                        System.out.println("What age is this victim? ");
                                        int age = scan.nextInt();
                                        System.out.println("Please enter a SSN in format (XXX-XX-XXXX), where the Xs are numbers and including the hyphens: ");
                                        String newSSN = scan.next();
                                        System.out.println("Enter an address (Format Ex: 1924 StreetName Ave): ");
                                        scan.nextLine();
                                        String newAddy = scan.nextLine();
                                        System.out.println("First name of victim: ");
                                        String newFirst = scan.next();
                                        System.out.println("Last name of victim: ");
                                        String newLast = scan.next();
                                        newVic.setString(1, desc);
                                        newVic.setInt(2, age);
                                        newVic.setString(3, newSSN);
                                        newVic.setString(4, newAddy);
                                        newVic.setString(5, newFirst);
                                        newVic.setString(6, newLast);
                                        newVic.executeUpdate();
                                        System.out.println("New victim has been added");
                                        break;
                                    case 4:
                                        System.out.println("Please enter new email: ");
                                        String newEmailUpd = scan.next();
                                        newEmail.setString(1, newEmailUpd);
                                        newEmail.setString(2, signInName);
                                        newEmail.executeUpdate();
                                        System.out.println("New email set");
                                        break;
                                    case 5:
                                        System.out.println("PLease enter new password: ");
                                        String newPasswordSet = scan.next();
                                        newPassword.setString(1, newPasswordSet);
                                        newPassword.setString(2, signInName);
                                        newPassword.executeUpdate();
                                        System.out.println("New password set");
                                        break;
                                    case 6:
                                        rs = stmt.executeQuery("SELECT * FROM commentGetIds");
                                        System.out.println("CommentIDs");
                                        while(rs.next())
                                        {
                                            System.out.println("Comment ID: " + rs.getInt("commentID") + " " + "Content: " + rs.getString("Content"));
                                        }
                                        System.out.println("Enter commentID of comment you wish to edit");
                                        int commentIdScan = scan.nextInt();
                                        System.out.println("Enter comment edit");
                                        scan.nextLine();
                                        String newCommentEdit = "EDIT: " + scan.nextLine();

                                        newComment.setString(1, newCommentEdit);
                                        newComment.setInt(2, commentIdScan);
                                        newComment.executeUpdate();
                                        System.out.println("Comment editted");
                                        break;
                                    case 7:
                                        //Remove Victim CHECK
                                        rs = stmt.executeQuery("CALL getAllVictims");
                                        System.out.println("Victims Removable");
                                        while(rs.next())
                                        {
                                            System.out.println("First Name: " + rs.getString("firstName") + " Last Name: " + rs.getString("lastName") + " SSN: " + rs.getString("SSN"));
                                        }
                                        System.out.println("Enter SSN of victim to remove");
                                        String SSNtoRemove = scan.next();
                                        deleteVictim.setString(1, SSNtoRemove);
                                        deleteVictim.executeUpdate();
                                        break;
                                    case 8:
                                        //Delete account CHECK
                                        System.out.println("Are you sure? (Y/N) ");
                                        if(scan.next().equalsIgnoreCase("y"))
                                        {
                                            deleteAccount.setString(1, signInName);
                                            deleteAccount.executeUpdate();
                                            System.out.println("Account deleted, goodbye!");
                                        }
                                        break;
                                    case 9:
                                        viewVictims.executeQuery();
                                        break;
                                    case 10:
                                        exit = !exit;
                                        System.out.println("See you soon, " + signInName);
                                        break;
                                } // End Option Switch
                                if(option == 8)
                                {
                                    break;
                                }
                            } // End Signed in
                        } //User exists end
                        break;
                    case 2:
                        System.out.println("Enter a username (50 character limit)");
                        String newUser = scan.next();
                        System.out.println("Enter an un-used email");
                        String newEmailSetter = scan.next();
                        rs = stmt.executeQuery("SELECT Email FROM Member WHERE Member.Email = '" + newEmailSetter + "'");
                        if(rs.next())
                        {
                            System.out.println("Sorry this email has been used.");
                            break;
                        }
                        System.out.println("Enter a password (20 character limit)");
                        String newPasswordSetter = scan.next();
                        registration.setString(1, newUser);
                        registration.setString(2, newEmailSetter);
                        registration.setString(3, newPasswordSetter);
                        registration.executeUpdate();
                        break;
                    case 3:
                        exit = !exit;
                       // break;
                } // Switch end


            } /*Try*/ catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                    } // ignore
                    rs = null;
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                    } // ignore
                    stmt = null;
                }
            } //Catch and finally
        } //While loop
    } // End main
}