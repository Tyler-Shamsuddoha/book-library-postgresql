/*
 * LibraryModel.java
 * Author: Tyler Shamsuddoha - Victoria University of Wellington - 300428076
 * Date: 2019
 */



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;



public class LibraryModel {

	// For use in creating dialogs and making them modal
	private JFrame dialogParent;
	private String userPassword;
	private String userID;
	private String myURL;
	private Connection con;
	
	/** Comment the following line to enable changes to the database (used for testing) **/
	private boolean TEST_MODE = true;

	private ImageIcon icon = new ImageIcon("Icon.png");

	public LibraryModel(JFrame parent, String userid, String password) {
		dialogParent = parent;
		this.userPassword = password;
		this.userID = userid;
		this.myURL = "jdbc:postgresql://localhost:5432/test";
		//	"jdbc:postgresql://db.ecs.vuw.ac.nz/shamsutyle_jdbc";

		try {
			con = DriverManager.getConnection(myURL,this.userID,this.userPassword);
			if(TEST_MODE == true) {
				con.setAutoCommit(false);
			}
		} catch (SQLException e) {

			System.out.println("Error connecting to the database.");
			JOptionPane.showMessageDialog(dialogParent, e.toString(), "An Error Occured",JOptionPane.ERROR_MESSAGE, this.icon);
			closeDBConnection();
			System.exit(-1);
		}
		System.out.println("Connection successful");
	}

	public String bookLookup(int isbn) {

		if(isbn < 0) {
			return "Invalid ISBN number. The ISBN number can not be less than 0.";
		}

		//***  Make sure to use NATURAL JOIN not Join, otherwise it won't work. ***///
		String Query1 = "SELECT ISBN, TITLE, Edition_No AS Edition, NumOfCop, NumLeft " + "FROM book " + "WHERE ISBN =" + isbn + ";";
		boolean isbn_Is_Valid= false; //check if real isbn num
		String output = "";
		String authorInfo = "";
		boolean multipleAuthors = false;
		try {

			//Execute the first Query (Query1) and find the first result set.
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);

			//Loop through all the rows in the result set for book, if there is a row that is returned then the ISBN is valid.
			while (rs1.next()) {
				//Book Title
				output = ("Book Lookup:\n" + "\t" + rs1.getInt("isbn") + ": " + rs1.getString("title") + "\n"
						+ "\tEdition: " + rs1.getInt("Edition") + " - Number of copies: " + rs1.getInt("NumOfCop") + " - Copies Left: "
						+ rs1.getInt("NumLeft") + "\n");

				//Since rows has been returned, the isbn is a valid isbn in the library.
				isbn_Is_Valid = true;
			}

			//Create the second Query for author info
			String Query2 = "SELECT surname FROM book NATURAL JOIN book_author NATURAL JOIN author "
					+ "WHERE isbn =" + isbn + ";";

			//Using the first result set, determine the other information that is needed to be displayed.
			Statement s2 = con.createStatement();
			ResultSet rs2 = s2.executeQuery(Query2);

			int amount_of_authors = 0;
			while(rs2.next()) {
				String author_name = rs2.getString("surname").trim();
				if(amount_of_authors >= 1) {
					multipleAuthors = true;
					authorInfo += ",";
					authorInfo += " ";
				}
				authorInfo += (author_name);
				amount_of_authors++;
			}

			//	If ISBN is not valid, the ISBN is not in the library.
			//	Print everything from the output.
			
			if(isbn_Is_Valid == false) {
				output = ("\tNo such ISBN: " + isbn);
			}
			else {
				if(amount_of_authors == 0) {
					output += ("\t(No Authors)");
				} else {
					output += "\tAuthor";

					if(multipleAuthors == true) {
						output+=("s");
					}
					output+=(": "+ authorInfo);
				}
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return output;
	}

	public String showCatalogue() {

		String authorsInfo= "";
		String output = "";
		try {
			output = ("Show Catalogue\n\n");

			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery("SELECT isbn, title, edition_no AS Edition, numofcop, numleft FROM book ORDER BY isbn;");

			while(rs1.next()) {
				output+=("\n\t" + rs1.getInt("isbn")
				+ ": " + rs1.getString("title") + "\n\t\tEdition: " + rs1.getInt("Edition")
				+ " - Number of copies: " + rs1.getInt("numofcop")
				+ " - Copies left: " + rs1.getInt("numleft") + "\n");

				Statement s2 = con.createStatement();
				ResultSet rs2 = s2.executeQuery("SELECT isbn, surname FROM book NATURAL JOIN book_author NATURAL JOIN author WHERE isbn = " + rs1.getInt("isbn") + ";");

				int count = 0;
				while(rs2.next()) {
					if(count > 0) {
						authorsInfo+=(", ");
					}
					authorsInfo+=(rs2.getString(2).trim());
					count++;
				}

				if(count > 0) {
					output += ("\t\tAuthor");

					if(count > 0) {
						output +=("s");
					}

					output +=": " + authorsInfo;
				} else {
					output += ("\t\t(No Authors)");
				}

				count = 0;
				authorsInfo = "";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}



	public String showLoanedBooks() {

		int counta = 0;
		int authorCount = 0;
		ResultSet rs1;
		ResultSet rs2;
		ResultSet rs3;
		boolean results = false;

		String output = "";
		String authorInfo ="";
		String borrowerInfo ="";

		try {

			output = "Show Loaned Books:\n";
			authorInfo = "";
			borrowerInfo = "";

			Statement	s1 = con.createStatement();
			rs1 = s1.executeQuery("select * from book NATURAL JOIN cust_book ORDER BY isbn asc;");

			while(rs1.next()) {

				int isbn = rs1.getInt("isbn");
				output += "\n\t" + isbn
						+ ": " + rs1.getString("title") + "\n" + "\t\t" + "Edition:" + " " + rs1.getInt("edition_no") + " - " + "Number of copies: " + rs1.getInt("numofcop") + " - " + "Copies left: " + rs1.getInt("numleft") + "\n";
				Statement s2 = con.createStatement();
				rs2 = s2.executeQuery("SELECT surname, isbn, title FROM book NATURAL JOIN book_author NATURAL JOIN author WHERE isbn = " + isbn + ";");

				while(rs2.next()) {
					if(counta >= 1) {
						authorInfo += ", ";
						authorCount++;
					}
					authorInfo += (rs2.getString("surname").trim());
					counta++;
				}

				if(counta >= 1) {
					output += "\t\tAuthor";

					if(authorCount >= 1) {
						output += "s";
					}

					output += ": " + authorInfo;
				} else {
					output +="\t\tno authors";
				}

				output += "\n";
				counta = 0;
				authorCount = 0;
				authorInfo = "";
				Statement s3 = con.createStatement();
				rs3 = s3.executeQuery("select customerid, l_name, f_name, city, isbn FROM customer NATURAL JOIN cust_book WHERE isbn = " + isbn + ";");

				output += "\tBorrowers:\n";
				while(rs3.next()) {
					borrowerInfo += "\t\t" + rs3.getInt(1) + ": " + rs3.getString(2).trim() + ", " + rs3.getString(3).trim() + " - ";

					String city  = "no city";
					if(rs3.getString(4) != null) {
						city = rs3.getString(4).trim();
					}
					borrowerInfo += city + "\n";
				}
				output += borrowerInfo;
				borrowerInfo = "";
				results = true;
			}
			if(results == false) {
				output += ("\tno Loaned Books");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return output;
	}




	public String showAuthor(int authorID) {

		if(authorID < 0) {
			return "Invalid authorID. The authorID can not be less than 0.";
		}

		String Query1 = "SELECT authorid, name, surname, isbn, title FROM book NATURAL JOIN book_author NATURAL JOIN author WHERE authorid = " + authorID + ";";
		String bookInfo = "";
		String output = "";
		boolean resultExists = false;
		boolean multipleBooks = false;

		try {

			output = "Show Author:\n";
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);

			while(rs1.next()) {
				if(resultExists == false) {
					output += ("\t" + rs1.getInt(1)
					+ " - " + rs1.getString("name").trim() + " " + rs1.getString("surname").trim() + "\n");

					resultExists = true;
				} else {
					multipleBooks = true;
					bookInfo += ("\n");
				}
				bookInfo += ("\t\t" + rs1.getInt("isbn") + " - " + rs1.getString("title").trim());
			}
			if(resultExists == false) {
				output+= "\tThe selected author ID of " + authorID + "does not exist.";
			} else {
				output+=("\tBook");

				if(multipleBooks == true) {
					output+=("s");
				}
				output+= " written:\n" + bookInfo;
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output;
	}


	public String showAllAuthors() {

		String output = "";
		String Query1 = "SELECT authorid, surname, name "
				+ "FROM author "
				+ "ORDER BY authorid;";

		try {

			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);
			output = "Show All Authors:\n";

			while(rs1.next()) {

				output += "\t" + rs1.getInt("authorid") + ": " + rs1.getString("surname").trim()
						+ ", " + rs1.getString("name").trim() + "\n";
			}
		}catch(SQLException e) {

			e.printStackTrace();
		}
		return output;
	}

	public String showCustomer(int customerID) {

		if(customerID < 0 ) {
			return "Invalid Customer ID";
		}

		String Query1 = "select * from customer where customerID = " + customerID + ";";
		String Query2 = "select cb.isbn, title from cust_book cb natural join book where customerid = " + customerID + ";";
		String output = "";
		String bookDetails = "";
		String city = "";

		try {

			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);

			while(rs1.next()) {
				output = "Show Customer:\n";
				output += "\t" + rs1.getInt("customerID") + ": " + (rs1.getString("L_Name").trim()+ ", " + rs1.getString("F_Name").trim())
						+ " - ";
				//https://stackoverflow.com/questions/5991360/handling-the-null-value-from-a-resultset-in-java
				city = rs1.getString("city");
				if(rs1.wasNull()) {
					city = "No city avaliable";
				}
				output += city + "\n" + "\tBorrowed Books:\n";
			}

			if(output.equals("")) {
				return "This customerID does not exist in the database yet.";
			}

			Statement s2 = con.createStatement();
			ResultSet rs2 = s2.executeQuery(Query2);

			while(rs2.next()) {
				bookDetails = rs2.getInt("isbn") + " - " + rs2.getString("title") + "\n";

			}
			if(bookDetails.equals("")) {
				bookDetails = "No Borrowed Books";
			}
			output += "\t\t" + bookDetails.trim();

		}catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}

	public String showAllCustomers() {
		String Query1 = "select * from customer;";
		String output = "Show All Customers:\n";

		try {
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);
			while (rs1.next()) {
				output += "\t" + rs1.getInt("customerID") + ": " + rs1.getString("f_name").trim()
						+ ", " + rs1.getString("l_name").trim() + "\n";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}


	public String borrowBook(int isbn, int customerID, int day, int month, int year) {

		if(customerID < 0 ) {
			return "Invalid Customer ID. CustomerID can not be less than 0.";
		}
		if(isbn < 0) {
			return "Invalid ISBN number. The ISBN number can not be less than 0.";
		}
		if(day > 31 || day < 0 || month > 12 || month < 0 || year < 0) {
			return "Invalid Date";
		}

		boolean isbn_is_valid = false;
		boolean validCustomer = false;
		String output = "";
		String bookName = "";

		try {
			Statement s1 = con.createStatement();

			//Query for specific book (by ISBN) returns details about that book eg. how many are left, title etc.
			ResultSet rs1 = s1.executeQuery("SELECT isbn, numleft, title "
					+ "FROM book WHERE isbn = " + isbn + ";");

			int booksLeft = 0;

			while(rs1.next()) {

				booksLeft = rs1.getInt("numLeft");

				if(booksLeft< 1) {
					
					//	If there is no books you cant borrow a book.
					
					return "There is not enough copies of the book" + rs1.getInt("isbn") + ": " + rs1.getString("Title");
				}

				bookName = rs1.getString("title").trim();
				isbn_is_valid = true; // turns true because it has at least one result that has been returned.
			}


			if(isbn_is_valid == false) {
				return ("\tNo such ISBN: " + isbn);
			}

			String Query2 = "SELECT customerid, f_name, l_name FROM customer WHERE customerid = " + customerID + ";";
			Statement s2 = con.createStatement();
			ResultSet rs2 = s2.executeQuery(Query2);
			String customerName = "";

			while(rs2.next()) {
				customerName = rs2.getString("f_name").trim() + " " + rs2.getString("l_name").trim();
				validCustomer = true;
			}

			if(validCustomer == false) {
				return ("\tNo such customer ID: " + customerID);
			}

			output += "\tBook: " + isbn + " " + bookName + "\n"
					+ "\tLoaned to: " + customerID + " (" + customerName + ")\n" +
					"\tDue Date: " + day + " " + getMonthAsString(month) + year;

			String date= year + "-" + month + "-" + day;

			PreparedStatement s3 = con.prepareStatement("INSERT INTO cust_book "
					+ "VALUES (" + isbn + ", date'" + date + "', " + customerID + ");");
			PreparedStatement s4 = con.prepareStatement("UPDATE book "
					+ "SET numleft = " + (booksLeft - 1)
					+ " WHERE isbn = " + isbn + ";");

			//https://stackoverflow.com/questions/24970176/joptionpane-handling-ok-cancel-and-x-button

			int a = JOptionPane.showOptionDialog(new JFrame(), "Confirm Borrow:\n\t" + customerName + " (CustomerID: " + customerID + ")\n\tPress YES to continue. Press NO to cancel."
					, "CONFIRM BORROW", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"Yes", "No"}, JOptionPane.YES_NO_OPTION);


			if(a == JOptionPane.YES_OPTION) {
				s3.executeUpdate();
				s4.executeUpdate();
				return "Success.\n" + output;

			}else if(a == JOptionPane.NO_OPTION) {
				return ("Borrow has been cancelled.");
			}else if(a == JOptionPane.CLOSED_OPTION) {
				return ("Window has closed\n\tBorrow has been cancelled.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return "Error deleting customer";
		}
		return "error";
	}

	public String returnBook(int isbn, int customerid) {
		if(customerid < 0 ) {
			return "Invalid Customer ID. CustomerID can not be less than 0.";
		}
		if(isbn < 0) {
			return "Invalid ISBN number. The ISBN number can not be less than 0.";
		}

		int countISBN = 0;
		int customerCount = 0;
		int loanCount = 0;

		PreparedStatement s4;
		PreparedStatement s5;
		ResultSet rs1;
		ResultSet rs2;
		ResultSet rs3;

		String output = "";

		try {

			output = "Return Book:\n";
			Statement s1 = con.createStatement();
			rs1 = s1.executeQuery("SELECT isbn, numleft, title "
					+ "FROM book "
					+ "WHERE isbn = " + isbn + ";");

			String bookName = "";
			int booksLeft = 0;
			while(rs1.next()) {
				booksLeft = rs1.getInt("numleft");
				bookName = rs1.getString("title");
				countISBN++;
			}

			if(countISBN == 0) {
				output += "\tNo such ISBN: " + isbn;
				return output;
			}

			Statement s2 = con.createStatement();
			rs2 = s2.executeQuery("SELECT customerid, f_name, l_name "
					+ "FROM customer "
					+ "WHERE customerid = " + customerid + ";");

			String custFullName = "";
			while(rs2.next()) {
				custFullName = rs2.getString("f_name").trim() + " " + rs2.getString("l_name").trim();
				customerCount++;
			}
			if(customerCount == 0) {
				output += "\tNo such customer ID: " + customerid;
				return output;
			}
			Statement s3 = con.createStatement();
			rs3 = s3.executeQuery("SELECT isbn, customerid "
					+ "FROM cust_book "
					+ "WHERE isbn = " + isbn + " AND customerID = " + customerid + ";");

			while(rs3.next()) {
				loanCount++;
			}

			if(loanCount == 0) {
				output += "\tBook " + isbn + " is not loaned to customer " + customerid;
				return output;
			}

			s4 = con.prepareStatement("DELETE FROM cust_book WHERE isbn = " + isbn + " and customerid = " + customerid + ";");
			s5 = con.prepareStatement("UPDATE book SET numleft = " + (booksLeft + 1) + "WHERE isbn = " + isbn + ";");

			int answer = JOptionPane.showOptionDialog(new JFrame(), "Confirm Return:\n\t" + custFullName.trim()  + " returning book " + isbn + " :" + bookName + "\nPress YES to continue. Press NO to cancel."
					, "CONFIRM RETURN", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, this.icon, new Object[] {"Yes", "No"}, JOptionPane.YES_NO_OPTION);

			if(answer == JOptionPane.YES_OPTION) {
				s4.executeUpdate();
				s5.executeUpdate();
				return (output + "\nSuccess. Customer " + customerid + ": " + custFullName + " has returned the book: " + isbn + " :" + bookName);
			}else if(answer == JOptionPane.NO_OPTION) {
				return (output + "\nCancelled returning the book " + isbn + " for customer " + customerid + ": " + custFullName);
			}else if(answer == JOptionPane.CLOSED_OPTION) {
				return (output + "\nWindow was closed\n\t" + "Cancelled returning the book " + isbn + " for customer " + customerid + ": " + custFullName);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output;
	}


	private String getMonthAsString(int month) {
		String monthInString = "";

		switch(month) {
		case 0:
			monthInString = "January" + " ";
			break;
		case 1:
			monthInString = "Febuary" + " ";
			break;
		case 2:
			monthInString = "March" + " ";
			break;
		case 3:
			monthInString = "April" + " ";
			break;
		case 4:
			monthInString = "May" + " ";
			break;
		case 5:
			monthInString = "June" + " ";
			break;
		case 6:
			monthInString = "July" + " ";
			break;
		case 7:
			monthInString = "August" + " ";
			break;
		case 8:
			monthInString = "September" + " ";
			break;
		case 9:
			monthInString = "October" + " ";
			break;
		case 10:
			monthInString = "November" + " ";
			break;
		case 11:
			monthInString = "December" + " ";
			break;
		default:
			monthInString = "Invalid month";
			break;
		}
		return monthInString;
	}

	public void closeDBConnection() {
		try {
			this.con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String deleteCus(int customerID) {

		if(customerID < 0) {
			return "Invalid customerID. The customerID can not be less than 0.";
		}

		String Query1 = "select * from Customer C WHERE C.CustomerId = " + customerID +";";
		String Query2 =  "DELETE FROM Customer WHERE CustomerId = " + customerID + ";";
		PreparedStatement s2;
		try {
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);
			if(rs1.next()) {
				s2 = con.prepareStatement(Query2);
			}else {
				return "There is no customer with that ID.";
			}
			String customerName = rs1.getString("f_name").trim() + " " + rs1.getString("l_name").trim();

			int a = JOptionPane.showOptionDialog(new JFrame(), "Confirm Delete:\n\t" + customerName + " (CustomerID: " + customerID + ")\nPress YES to continue. Press NO to cancel."
					, "CONFIRM DELETION", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"Yes", "No"}, JOptionPane.YES_NO_OPTION);

			if(a == JOptionPane.YES_OPTION) {
				s2.executeUpdate();
				return ("Success. The Customer " + customerName + " with a customerID of: " + customerID +" has been deleted.");
			}else if(a == JOptionPane.NO_OPTION) {
				return ("Deletion of customer " + customerName + " with a customerID of: " + customerID + " has been cancelled.");
			}else if(a == JOptionPane.CLOSED_OPTION) {
				return ("Window was closed\n\tDeletion of customer " + customerName + " with a customerID of: " + customerID + " has been cancelled.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error deleting customer";
		}
		return "Error has occurred";
	}

	public String deleteAuthor(int authorID) {

		if(authorID < 0) {
			return "Invalid authorID. The authorID can not be less than 0.";
		}

		String Query1 = "select * from Author A WHERE A.authorID = " + authorID +";";
		String Query2 =  "delete from Author A WHERE A.authorID = " + authorID + ";";
		PreparedStatement s2;

		try {
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);
			if(rs1.next()) {
				s2 = con.prepareStatement(Query2);
			}else {
				return "There is no author with that ID.";
			}

			String authorFullName = rs1.getString("name").trim() + " " + rs1.getString("surname").trim();

			int a = JOptionPane.showOptionDialog(new JFrame(), "Confirm Delete:\n\t" + authorFullName.trim() + " (AuthorID: " + authorID + ")\nPress YES to continue. Press NO to cancel."
					, "CONFIRM DELETION", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, this.icon, new Object[] {"Yes", "No"}, JOptionPane.YES_NO_OPTION);

			if(a == JOptionPane.YES_OPTION) {
				s2.executeUpdate();
				return ("Success. The author " + authorFullName + " with a authorID of: " + authorID +" has been deleted.");
			}else if(a == JOptionPane.NO_OPTION) {
				return ("Cancelled the deletion of customer " + authorFullName + " with a authorID of: " + authorID);
			}else if(a == JOptionPane.CLOSED_OPTION) {
				return ("Window was closed\n\tCancelled the deletion of author " + authorFullName + " with a authorID of: " + authorID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error deleting customer";
		}
		return "Error has occurred";
	}


	public String deleteBook(int isbn) {
		if(isbn < 0) {
			return "Invalid ISBN number. The ISBN number can not be less than 0.";
		}

		String Query1 = "select * from book B WHERE B.isbn = " + isbn +";";
		String Query2 =  "delete from Book B where B.isbn = " + isbn + ";";

		PreparedStatement s2;
		try {
			Statement s1 = con.createStatement();
			ResultSet rs1 = s1.executeQuery(Query1);
			if(rs1.next()) {
				s2 = con.prepareStatement(Query2);
			}else {
				return "There is no book with that ISBN.";
			}
			String bookNameAndISBN = rs1.getString("isbn").trim() + ": " + rs1.getString("title").trim();

			int a = JOptionPane.showOptionDialog(new JFrame(), "Confirm Delete:\n\t" + bookNameAndISBN + "\n Press YES to continue. Press NO to cancel."
					, "CONFIRM DELETION", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"Yes", "No"}, JOptionPane.YES_NO_OPTION);

			if(a == JOptionPane.YES_OPTION) {
				s2.executeUpdate();
				return ("Success. The book " + bookNameAndISBN + "has been deleted.");
			}else if(a == JOptionPane.NO_OPTION) {
				return ("Cancelled the deletion of " + bookNameAndISBN + ".");
			}else if(a == JOptionPane.CLOSED_OPTION) {
				return ("Window was closed\n\t" + "Cancelled the deletion of " + bookNameAndISBN + ".");
			}
		} catch (SQLException e) {
			return "Error deleting book, could be because book is loaned to a customer.";
		}
		return "Error has occurred";
	}
}
