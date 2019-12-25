## Test plan

In this project, we conducted two major categories of tests: test of database (DB) functions and test of user functions.

### Part I. Test of DB Functions

In this part, we tested DB functions by checking constraints of each table. 

We can run test of DB Functions by running `Test.java`.

The test of DB functions is inialized by:

- creating tables by calling `Tables.initializeAll()`.
- Inserting test movies by calling `Test.insertTestMovies()`.

For each table: `Customer, Attendance, Review, Endorsement`, we conducted one separate test.

- Each test contains a valid test case on all constraints. 

- For each constraint, there is an invalid input test case. Corresponding error messages are printed indicating the data and method that caused the error.

#### 1. Check constraints for Table `Customer`

- Insert 3 valid customers. 

- Check constraint: invalid email format

#### 2. Check constraints for Table `Attendance`

- Insert 3 valid attendances.
- Check constraint: a customer cannot attend a movie before registration.

#### 3. Check constraints for Table `Review`

- Insert 2 valid reviews.
- Check constraint: date of the review must be within 7 days of the most recent attendance of the movie.
- Check constraint: There can only be one movie review per customer.
- Check constraint: `rating` must be between 0 and 5.

#### 4. Check constraints for Table `Endorsement`

- Insert 1 valid endorsement.
- Check constraint: a customer cannot endorse his or her own review.
- Check constraint: the review should be opened for voting.
- Check constraint: a customer cannot endorse more than one review for a movie within one day.



---

### Part II. Test of User Functions

In this part, we tested user functions of the project: customer functions and theater functions. 

You can run test of DB Functions by running `MainPage.java`. You will encounter a welcome page:

```
--------------------------------------
WELCOME TO IRATE MOVIE RATING SYSTEM
--------------------------------------
1. Initialize all tables
2. Load sample data
3. Print all tables
4. Log in as customer
5. Log in as an employee at theater
6. Exit program
Enter your choice: 

```

You will first initialize the test by:

- Creating tables by choosing `1`.
- Loading sample data by choosing `2`

Then you can test following functions by following the instructions in `/Tests/Test_result_part2.md`.

#### Customer Functions

- Account management: login, register, show account info, delete account
- Activity: 
  - check movie attendance, 
  - write a review of a watched movie, 
  - endorse other customers' review
- Explore Data: 
  - print all movies
  - get top-three-rated movies

#### Theater Functions

- Movie management: 
  - add movie, 
  - delete movie
- Theater Activity: 
  - select an author for the top-rated review
  - select a lucky customer who voted on a certain day
  - select a lucky customer who voted on a certain day
- Explore data: 
  - print all movies
  - print all reviews ordered by number of endorsement
  - Print all customers who voted on a certain day

