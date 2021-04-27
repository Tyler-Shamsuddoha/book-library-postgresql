# Contents


### **src**  
LibraryModel.java - was edited by me and contains the SQL queries  
LibraryUI.java - Application UI

### **create_library.data**  
Use command  
```
psql -d <db_name> -f ~/create_library.data
```
This will execute the CREATE TABLE and INSERT commands and populate the database accordingly.  
  
It is important to note that you must have a specified database to run this command, although  creating a new database is relatively self explanatory, should one need assistance refer to **Project_19.pdf** for instructions  
  
## Functionality

1. Book
   - Book Lookup
   - Show Catalogue
   - Show Loaned Books
3. Author
   - Show Author
   - Show All Authors
5. Customer
   - Show Customer
   - Show All Customers
7. Borrow Book
9. Return Book
10. File
    - Exit  
  
![image](https://user-images.githubusercontent.com/79490285/116324773-bb082580-a78e-11eb-855a-b845fdc99d53.png)  
  
Example of Show Catalogue  
  
![image](https://user-images.githubusercontent.com/79490285/116324857-e9860080-a78e-11eb-82de-a3610569909f.png)  
  
Example of Show All Customers

![image](https://user-images.githubusercontent.com/79490285/116324270-d7579280-a78d-11eb-89cb-06858ce1d256.png)
  
Example of Cutomer Kirk Jackson (CustomerID: 1) looking up Fundamentals of Database Systems (ISBN: 1111) and then issuing it. Note that after the book has been loaned to Kirk Jackson, when it is looked up again, the amount of 'Copies Left' has decreased as it has been loaned out.

![image](https://user-images.githubusercontent.com/79490285/116324568-61076000-a78e-11eb-9cb6-5dbb0f6a99d3.png)  
  
Kirk Jackson (CustomerID: 1) returning book Fundamentals of Database Systems (ISBN:1111)
