import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This program simulates a library and a set borrowers.
 * Each borrower borrows one book at a time, reads it
 * until the loan period ends, and then returns the book
 * to the library. 
 * 
 * The problem is to implement the Borrower.borrowBook() 
 * and Borrower.returnBook() methods that are called from 
 * Borrower.run(). Complete descriptions of the algorithms 
 * and messages produced by the methods are in the method 
 * comment blocks. 
 * file has sample output. Actual output may vary due to
 * differences in thread scheduling.
 */
public class Library {
	/** library instance */
	static Library library;
	
	/**
	 * This class represents a library. Access to the circulation 
	 * functions should be locked by the circulation object.
	 */
	static class Library {
		/** collection of books */
		private List<String> books;

		/** book circulation period */
		final public int circulationPeriod = 2;
		
		/** library closing time */
		final public int closingTime = 7;
		
		/** lock for circulation functions */
		final public Object circulation = new Object();

        /**
         * Create a library with nbooks books.
         *
         * @param nbooks the number of books
         */
		Library(int nbooks) {
			books = new ArrayList<String>(nbooks);
			for (int i = 0; i < nbooks; i++) {
				books.add("Book " + i);
			}
		}
		
		/**
		 * Recommend a book to check borrow. To ensure the book
		 * remains available to borrow, access to this function
		 * should be locked by the circulation object.
		 *  
		 * @return a recommended book
		 */
		String recommendBook() {
			Iterator<String> itr = books.iterator();
			if (itr.hasNext()) {
				String book = itr.next();
				return book;
			}
			return null;
		}
		
		/**
		 * Borrow a book.
		 * 
		 * @param book the book
		 * @return true if the book was checked out and false if
		 *   it was unavailable
		 */
		boolean borrowBook(String book) {
			if (books.contains(book)) {
				books.remove(book);
				return true;
			}
			return false;
		}

		/**
		 * Return a book.
		 * 
		 * @param book the book to return
		 * @return true if the book was returned and false if
		 *   is not checked out
		 */
		boolean returnBook(String book) {
			if (books.contains(book)) {
				return false;
			}
			books.add(book);
			return true;
		}
	}
	
	/**
	 * This class represents a library borrower. The run method
	 * simulates time passing. Each iteration of the loop is a 
	 * time period. It begins with a sleep for 1 second to slow
	 * the simulation. The loop terminates when the current time
	 * is after the library closing time. The borrower reports how 
	 * many books were borrowed as "finished <borrowedCount> books".
	 * 
	 * NOTE: Access to the library circulation functions should be 
	 * locked by the library.circulation object.
	 */
	static class Borrower implements Runnable {
		final int id;  			// the borrower ID
		String book = null;     // checked out book
		int dueBy = 0;          // due by time for the book
		int currentTime = 0;    // the current simulated time
		int borrowedCount = 0;  // the number borrow operations
		
		/**
		 * Initialize the borrower with a borrower id.
		 * @param id the borrower id
		 */
		Borrower(int id) {
			this.id = id;
		}
		
		/**
		 * Report borrower message.
		 * @param message the message to report
		 */
		public void report(String message) {
			System.out.printf("Borrower %d [%2d]: %s\n", id, currentTime, message);
		}
		
		/**
		 * Borrow a book from the library.
		 * 	 
		 * If the current time is before the library closing time, 
		 * the borrower uses a critical section to get a recommendation 
		 * from the library. If there is no recommendation, the borrower 
		 * reports "no book to borrow" for that time period. 
		 * 
		 * Otherwise, the borrower borrows the book, reports the loan as 
		 * "borrowed <bookname>", sets the due by time to the lesser of 
		 * the current time plus the library circulation period or the 
		 * library closing time, and increments the borrowed count 
		 * 
		 * The recommended book is only guaranteed to be available if 
		 * borrowed in the same critical section. If the borrow operation 
		 * fails because the book is unavailable, the borrower reports 
		 * "***unavailable: <bookname>"
		 */
		void borrowBook() {			
			// your code here
			// if current time is before library closing time
			if (currentTime >= library.closingTime) {
				return;
			}
			// access to recommend function should be locked by the circulation object.
			synchronized(library.circulation) {
				book = library.recommendBook();
				if (book == null) {
					report("no book to borrow");
				}
				else {
					if (library.borrowBook(book) == true) {
						report("borrowed " + book);
						dueBy = Math.min(currentTime + library.circulationPeriod, library.closingTime);
						borrowedCount ++;
					}
					else { // borrow operation fails
						report("***unavailable: " + book);
						book = null; // reset book to null because borrow failed	
					}
				}
			}
		}

		/**
		 * Return borrower's book to the library.
		 * 
		 * The borrower returns the book to the library within 
		 * a critical section, sets book to null, and reports 
		 * "returned <bookname>". If the return operation fails, 
		 * the borrower reports "***already returned <bookname>"
		 */
		void returnBook() {
			// your code here 
			synchronized(library.circulation) {
				if (library.returnBook(book) == true) {
					report("returned " + book);
					book = null;
				}
				else {
					report("***already returned " + book);
				}
			}
		}
		
		@Override
		public void run() {
			for ( ; currentTime <= library.closingTime; currentTime++) {
				// delay so output displays more slowly
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// do nothing
				}

				if (book == null) {  // no book
					borrowBook();
				} else if (currentTime >= dueBy) { // book is due
					returnBook();
				} else {  // still have book
					report("reading " + book);
				}
			}
			
			report("finished " + borrowedCount + " books");
		}
	}
	
	/**
	 * Create library and borrowers.
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		System.out.print("Start\n");

		final int nBorrowers = 4;  // number of borrowers

		// one fewer book than borrowers to test out of books condition
		library = new Library(nBorrowers-1);
		
		Thread threads[] = new Thread[nBorrowers];
		
		// create and run borrowers
		for (int id = 0; id < nBorrowers; id++) {
			// create borrower with id
			Borrower borrower = new Borrower(id);
			threads[id] = new Thread(borrower, "Borrower " + id);
			threads[id].start();
		}
		for (int id = 0; id < nBorrowers; id++) {
			try {
				threads[id].join();
			} catch (InterruptedException e) {
			}
		}

		System.out.print("End\n");
	}
}
