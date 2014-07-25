This example shows the handling of authorship with species level name when subspecies are present.

The 3 rows represent:
 i) Animalia,Aus,Aus bus,Aus bus cus		
ii) Animalia,Aus,Aus bus Tim Robertson 2010

Row i) does not have an author, but row ii) provides the authorship for the "Aus bus" species level name.  
The normalized concept for "Aus bus" will already exist when row ii) is handled, and therefore, this test ensures
that the authorship is correctly assigned to a previously created concept.

 