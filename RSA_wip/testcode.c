#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "number.h"

// Right now this file only has test code...

int testOne()
{
    bc_num bigNumOne = bc_new_num(62,0);
    bc_str2num( &bigNumOne, "1234567890987654321123456789009876543211234567890", 0 );

    bc_num bigNumTwo = bc_new_num(60,0);
    srand(time(NULL));
    for ( int i = 0; i < 60; i++ )
    {
        // Gabriel: NOTE: rand() is NOT cryptographically secure. This is just for test purposes only.
        bigNumTwo->n_value[i] = (char)(rand() % 10);
	printf( "%i", (int)bigNumTwo->n_value[i] );
    }
    printf("\n");
    printf("bigNumOne = %s (%i)\n", bc_num2str(bigNumOne), bigNumOne->n_len);
    printf("bigNumTwo = %s (%i)\n", bc_num2str(bigNumTwo), bigNumTwo->n_len);

    bc_num result = bc_new_num(100,0);
    bc_add(bigNumOne, bigNumTwo, &result, 0);

    char output[256];
    // Gabriel: NOTE: strcat is possibly vulnerable to buffer overflows. Don't use it in production code.
    strcat(output, "Hippity hoppity, the number ");
    strcat(output, bc_num2str(result));
    strcat(output, " is now your property.");
    printf("%s\n", output);
    return 0;
}

int main(int argc, char** argv)
{
	return testOne();
}
