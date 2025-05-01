%% Test Complexe operations %%

BEGIN Test5

  READ(number) ,              :: Read a number from user input
  a := number * number + 8 / 2 - 12 *(6 + 12 * 5),
  PRINT(a) ,
  b := ((number * number) + (8 / 2)) - (12 *(6 + (12 * 5))),
  PRINT(b),
END