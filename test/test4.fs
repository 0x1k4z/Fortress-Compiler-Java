%% Tests ASSIGN, READ, PRINT and IF/ELSE conditions. %%

BEGIN Test4

READ(number) ,              :: Read a number from user input

IF (number > -1) THEN
    number := number * number ,    :: square of number
ELSE                        :: The input number is negative
  number := number * number / 2 ,  :: half the square of number
END ,

PRINT(number) ,
END