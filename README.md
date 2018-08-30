# Library

This is a concurrent java program simulates a library and a set borrowers.
Each borrower borrows one book at a time, reads it until the loan period ends, 
and then returns the book to the library. 

The library has set a circulation time for a peirod, when borrowers access to the 
circulation functions, the curculation object should be locked by monitor lock to make
sure only one person is borrowing or returning at the same time.

The library has an iterator to recommend the next available book to borrowers, return 
null if no more books. The circulation object should be locked to get a recommended book.
Library could also check whether a book is successfully borrowed or returned.

Borrowers could only borrow book before library close time, and return the book
at due time/library close tiime. Each borrower uses a critical section to get a recommendation 
from the library. If there is no recommendation (no more books left), the borrower reports 
"no book to borrow" for that time period. Borrowers also need to return books through
the synchrinized circulation object.



