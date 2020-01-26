// REFACTOR THIS WHEN DONE!
// Should also have some asserts here and there...

// To compile:
// 	gcc -o ./rsa ./number.c ./rsa.c

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

#include "number.h"

// TODO: Refactor the code below into a separate file, since it's also needed for DSA.

bc_num zero;
bc_num one;
bc_num two;
bc_num six;
bc_num hundred;

// MUST call this.
void InitNums( void )
{
	zero = bc_new_num(1,0);
	one = bc_new_num(1,0);
	one->n_sign = PLUS;
	one->n_value[0] = 1;
	two = bc_new_num(1,0);
	two->n_sign = PLUS;
	two->n_value[0] = 2;
	six = bc_new_num(1,0);
	six->n_sign = PLUS;
	six->n_value[0] = 6;
	hundred = bc_new_num(1,0);
	bc_str2num( &hundred, "100", 0 );
}

// Gets a random number within a range.
// TODO: There's gotta be a better way of doing this...
bc_num GetRandomBCNumber(bc_num min, bc_num max)
{
	/*bc_num final = bc_new_num(max->n_len, 0);
	int iLength;
	do
	{
		srand( clock() );
		iLength = rand() % max->n_len;
		final->n_len = iLength;
		for ( int i = max->n_len-1; i >= max->n_len - 1 - iLength ; i-- )
		{
			final->n_value[i] = (char)(rand() % 10);	// FIXME: NOT CRYPTOGRAPHICALLY SECURE
		}
	}
	while ( bc_compare(final, min) < 0 || bc_compare(final, max) > 0 );

	return final;*/

	bc_num range = bc_new_num(1,0);	// Range of possible digits.
	bc_sub( max, min, &range, 0 );
	bc_add( range, one, &range, 0 );

	bc_num final = bc_new_num(range->n_len,0);
	int iLength;
	do
	{
		srand( clock() );
		iLength = rand() % range->n_len;
		for ( int i = range->n_len-1; i >= range->n_len - 1 - iLength; i-- )
		{
			final->n_value[i] = (char)(rand() % 10);	// FIXME: NOT CRYPTOGRAPHICALLY SECURE
		}
		bc_add( min, final, &final, 0 );
		//printf( "final = %s\n", bc_num2str(final) );
	}
	while ( bc_compare(final, max) > 0 );

	return final;
}

// Test to see if a number is composite.
// Used as part of the Miller-Rabin test.
// NOTE: Based off the Python code from:
// 	https://rosettacode.org/wiki/Miller-Rabin_primality_test
// I take no credit for that implementation; this is simply a transcription of it.
int TestComposite(bc_num* num, bc_num* a, bc_num* d, int s)
{
	bc_num numMinusOne = bc_copy_num(zero);
	bc_sub( *num, one, &numMinusOne, 0 );
	bc_num tempOne = bc_new_num(1,0);	// Temp variable.

	//printf( "TestComposite A %s %s %s\n", bc_num2str(*a), bc_num2str(*d), bc_num2str(*num) );
	// Check if (a^d mod num) = 1 or num - 1. If so, not composite.
	bc_raisemod( *a, *d, *num, &tempOne, 0 );
	if ( bc_compare(tempOne, one) == 0 || bc_compare(tempOne, numMinusOne) == 0 )
		return 0;

	// Check if [a^(2^i * d) mod num] == num - 1, where i is the current cycle.
	// If so, this isn't composite.
	bc_num i_big = bc_copy_num(zero);	// For calculations.
	for ( int i = 0; i < s; i++ )
	{
		bc_raise( two, i_big, &tempOne, 0 );
		bc_multiply( tempOne, *d, &tempOne, 0 );
		bc_raisemod( *a, tempOne, *num, &tempOne, 0 );
		if ( bc_compare(tempOne, numMinusOne) == 0 )
			return 0;

		bc_add( i_big, one, &i_big, 0 );
	}

	return 1;
}

// Miller-Rabin primality test.
// Returns 0 if the number is definitely composite and 1 if it's probably prime.
// NOTE: Based off the Python code from:
// 	https://rosettacode.org/wiki/Miller-Rabin_primality_test
// I take no credit for that implementation; this is simply a port of it.
int IsProbablyPrime(bc_num* num, int iRounds)
{
	assert((*num)->n_sign == PLUS);

	// First check if the number equals 0, 1, 2, 3, 4, or 5. They are different in terms of primality.
	int iPrimesUnderSix[] = {0, 0, 1, 1, 0, 1};
	if ( bc_compare(*num, six) < 0 )
	{
		return iPrimesUnderSix[ (*num)->n_value[0] ];
	}

	// Check if the last digit is evenly divisible.
	if ( (*num)->n_value[ (*num)->n_len-1 ] % 2 == 0 )
	{
		return 0;
	}

	// Find the odd value d.
	int s = 0;
	bc_num s_big = bc_copy_num(zero);	// For assertion.
	bc_num d = bc_copy_num(zero);
	bc_sub(*num, one, &d, 0);
	while ( d->n_value[ d->n_len-1 ] % 2 == 0 )
	{
		bc_divide( d, two, &d, 0 );
		s++;
		bc_add( s_big, one, &s_big, 0 );	// For assertion.
	}
	// ASSERT BEGINS HERE
	bc_num assertProduct = bc_copy_num(zero);
	bc_raise( two, s_big, &assertProduct, 0 );
	bc_multiply( assertProduct, d, &assertProduct, 0 );
	bc_num numMinusOne = bc_copy_num(*num);
	bc_sub( numMinusOne, one, &numMinusOne, 0 );
	assert( bc_compare(assertProduct, numMinusOne) == 0 );
	// ASSERT ENDS HERE
	
	// Test for composition.
	bc_num numMinusTwo = bc_copy_num(zero);
	bc_sub( *num, two, &numMinusTwo, 0 );
	bc_num a;
	for ( int i = 0; i < iRounds; i++ )
	{
		a = GetRandomBCNumber( two, numMinusTwo );
		if ( TestComposite( num, &a, &d, s ) )
		{
			return 0;
		}
	}

	// If we passed all the composition tests, probably the given number's a prime.
	return 1;
}



// Generates a random large prime number.
#define ROUNDS 16
int bAlreadyNotified = 0;
bc_num GetRandomBCPrime(int iBits)
{
	int i;

	// Create the minimum.
	bc_num min = bc_new_num(1,0);
	bc_str2num( &min, "6074001000", 0 );
	for ( i = 0; i < iBits - 33; i++ )
	{
		bc_multiply( min, two, &min, 0 );
	}
	// Create the maximum.
	bc_num max = bc_copy_num(one);
	for ( i = 0; i < iBits; i++ )
	{
		bc_multiply( max, two, &max, 0 );
	}
	bc_sub( max, one, &max, 0 );
	if ( !bAlreadyNotified )
	{
		printf( "min = %s\nmax = %s\n", bc_num2str(min), bc_num2str(max) );
		bAlreadyNotified = 1;
	}
	// min approx. = sqrt(2) * 2^2047
	// max = 2^2048 - 1
	//printf( "min = %s\nmax = %s\n", bc_num2str(min), bc_num2str(max) );
	
	// Keep trying to generate a prime number until we get one.
	// NOTE: This seems to be the slowest part...
	bc_num curVal;
	do
	{
		//srand( clock() );
		curVal = GetRandomBCNumber(min, max);
		//printf("testing %s...\n", bc_num2str(curVal));
	}
	while ( !IsProbablyPrime( &curVal, ROUNDS ) );

	return curVal;
}

// Finds the greatest common denominator between two BC numbers using Euclid's algorithm.
bc_num BC_GCD(bc_num a, bc_num b)
{
	//printf( "A\n" );
	bc_num curA = bc_copy_num(a);
	bc_num curB = bc_copy_num(b);
	//bc_num quotient = bc_new_num(1,0);
	bc_num remainder = bc_new_num(1,0);
	while ( 1 )
	{
		if ( bc_is_zero(curA) )
			return curB;
		if ( bc_is_zero(curB) )
			return curA;

		//printf( "B %s %s\n", bc_num2str(curA), bc_num2str(curB) );
		bc_modulo( curA, curB, &remainder, 0 );
		curA = bc_copy_num(curB);
		curB = bc_copy_num(remainder);
		//printf( "C %s %s\n", bc_num2str(curA), bc_num2str(curB) );
	}
}

// Finds the lowest common multiple of two numbers.
bc_num BC_LCM(bc_num a, bc_num b)
{
	bc_num out = bc_new_num(1,0);
	bc_divide( a, BC_GCD(a,b), &out, 0 );
	bc_multiply( out, b, &out, 0 );
	return out;
}

// Calculates the modular multiplicative inverse of a number.
// Ported from BigInteger.js:
// 	https://github.com/peterolson/BigInteger.js/blob/master/BigInteger.js
// I have no involvement with the original code.
// I'd have found out better how it works and written something more original, but I was on a tight schedule at the time of writing.
bc_num BC_ModInv(bc_num value, bc_num n)
{
	bc_num t = bc_copy_num(zero);
	bc_num newT = bc_copy_num(one);
	bc_num r = bc_copy_num(n);
	bc_num newR = bc_copy_num(value);
	bc_num q = bc_new_num(1,0);
	bc_num lastT = bc_new_num(1,0);
	bc_num lastR = bc_new_num(1,0);
	bc_num temp = bc_new_num(1,0);
	while ( !bc_is_zero(newR) )
	{
		bc_divide( r, newR, &q, 0 );
		lastT = bc_copy_num(t);
		lastR = bc_copy_num(r);
		t = bc_copy_num(newT);
		r = bc_copy_num(newR);
		bc_multiply( q, newT, &temp, 0 );
		bc_sub( lastT, temp, &newT, 0 );
		bc_multiply( q, newR, &temp, 0 );
		bc_sub( lastR, temp, &newR, 0 );
	}
	assert( bc_compare(r, one) == 0 );
	if ( bc_compare(t, zero) < 0 )
	{
		bc_add( t, n, &t, 0 );
	}
	if ( value->n_sign == MINUS )
	{
		t->n_sign = (t->n_sign == PLUS ? MINUS : PLUS);
	}
	return t;
}


// TODO: Non-RSA-specific code ends here.

// Generates an RSA keypair, based on the given keysize.
// Returns:
// 	n = modulus
// 	e = public key
// 	d = private key
void GenerateRSAKeypair( int iKeysize, bc_num *n, bc_num *e, bc_num *d )
{
	bc_num p, q, lambda;
	bc_str2num( e, "65537", 0 );	// Fixed public exponent.

	// Generating the threshold for the upcoming loop, since with bc numbers, there's no short way to do it.
	bc_num threshold = bc_new_num(1,0);	// Threshold for second condition in the loop.
	char szKeysize[16];
	sprintf( szKeysize, "%d", iKeysize );
	bc_str2num( &threshold, szKeysize, 0 );
	bc_divide( threshold, two, &threshold, 0 );
	bc_sub( threshold, hundred, &threshold, 0 );
	bc_raise( two, threshold, &threshold, 0 );

	// Generate p and q such that:
	// 	lambda(n) = lcm(p-1, q-1) shares no common factors with e
	// 	|p - q| >= 2^(bits/2 - 100)
	bc_num pMinusOne, qMinusOne;
	bc_num P_minus_Q = bc_new_num(1,0);
	do
	{
		printf("grabbing parameters...\n");

		p = GetRandomBCPrime( iKeysize / 2 );
		printf( "got p = %s\n", bc_num2str(p) );
		
		q = GetRandomBCPrime( iKeysize / 2 );
		printf( "got q = %s\n", bc_num2str(q) );
		
		pMinusOne = bc_copy_num(p);
		bc_sub( pMinusOne, one, &pMinusOne, 0 );
		qMinusOne = bc_copy_num(q);
		bc_sub( qMinusOne, one, &qMinusOne, 0 );
		lambda = BC_LCM( pMinusOne, qMinusOne );
		printf( "got lambda = %s\n", bc_num2str(lambda) );
		
		bc_sub( p, q, &P_minus_Q, 0 );
		P_minus_Q->n_sign = PLUS;
		
		printf("GCD(e,lambda): %s\n", bc_num2str(BC_GCD(*e,lambda)));
		printf("threshold comparison: %d\n", bc_compare(P_minus_Q, threshold));
	}
	while( bc_compare( BC_GCD(*e,lambda), one ) != 0 || bc_compare(P_minus_Q, threshold) < 0 );
	printf("Done!\n");

	// Returning the values here.
	bc_multiply( p, q, n, 0 );
	// e already returned
	*d = BC_ModInv(*e, lambda);
}

// Encrypt a message, which is in number format, using public exponent e and modulus n.
bc_num RSAEncrypt( bc_num m, bc_num e, bc_num n )
{
	bc_num c = bc_new_num(1,0);
	bc_raisemod( m, e, n, &c, 0 );
	return c;
}

// Decrypt a ciphertext, whose message is output in number format, using private exponent d and modulus n.
bc_num RSADecrypt( bc_num c, bc_num d, bc_num n )
{
	bc_num m = bc_new_num(1,0);
	bc_raisemod( c, d, n, &m, 0 );
	return m;
}


// Testing out the above.
int main(int argc, char** argv)
{
	//int const iRounds = 32;	// NOTE: Too few rounds and you may get back a false negative, or worse, a false positive.

	// These two MUST be called.
	InitNums();
	bc_init_numbers();
	
	// NOTE: I expected this to be slow and inefficient, but holy HELL did this defy my expectations. Imagine how much worse it'll be for mobile...
	// NOTE: There's gotta be some way to optimize it. Maybe consider the bases as well, for which we can use fewer values of A in the Miller-Rabin test?
	/*bc_num testNum = bc_new_num(666,0);
	bc_str2num( &testNum, "4547337172376300111955330758342147474062293202868155909489", 0 );
	printf( "Probably a prime? %i\n", IsProbablyPrime(&testNum, iRounds) );
	bc_str2num( &testNum, "4547337172376300111955330758342147474062293202868155909393", 0 );
	printf( "Probably a prime? %i\n", IsProbablyPrime(&testNum, iRounds) );
	bc_str2num( &testNum, "643808006803554439230129854961492699151386107534013432918073439524138264842370630061369715394739134090922937332590384720397133335969549256322620979036686633213903952966175107096769180017646161851573147596390153", 0 );
	printf( "Probably a prime? %i\n", IsProbablyPrime(&testNum, iRounds) );
	bc_str2num( &testNum, "743808006803554439230129854961492699151386107534013432918073439524138264842370630061369715394739134090922937332590384720397133335969549256322620979036686633213903952966175107096769180017646161851573147596390153", 0 );
	printf( "Probably a prime? %i\n", IsProbablyPrime(&testNum, iRounds) );

	bc_num gcdA = bc_new_num(1,0);
	bc_str2num( &gcdA, "270", 0 );
	bc_num gcdB = bc_new_num(1,0);
	bc_str2num( &gcdB, "192", 0 );
	printf( "GCD(%s, %s) = %s\n", bc_num2str(gcdA), bc_num2str(gcdB), bc_num2str( BC_GCD(gcdA, gcdB) ) );

	bc_str2num( &gcdA, "4547337172376300111955330758342147474062293202868155909489", 0 );
	bc_str2num( &gcdB, "4547337172376300111955330758342147474062293202868155909393", 0 );
	printf( "GCD(%s, %s) = %s\n", bc_num2str(gcdA), bc_num2str(gcdB), bc_num2str( BC_GCD(gcdA, gcdB) ) );*/
	
	//gcdA = GetRandomBCNumber(min, max);
	//gcdB = GetRandomBCNumber(min, max);
	//printf( "LCM(%s, %s) = %s\n", bc_num2str(gcdA), bc_num2str(gcdB), bc_num2str( BC_LCM(gcdA, gcdB) ) );
	
	bc_num n = bc_new_num(1,0);
	bc_num e = bc_new_num(1,0);
	bc_num d = bc_new_num(1,0);
	GenerateRSAKeypair( 512, &n, &e, &d );
	printf( "n = %s\n\ne = %s\n\nd = %s\n\n", bc_num2str(n), bc_num2str(e), bc_num2str(d) );

	bc_num testNum = bc_new_num(1,0);
	bc_str2num( &testNum, "12345678909876543210", 0 );
	printf( "message = %s\n", bc_num2str(testNum) );
	printf( "Encrypting...\n" );
	testNum = RSAEncrypt( testNum, e, n );
	printf( "ciphertext = %s\n", bc_num2str(testNum) );
	printf( "Decrypting...\n" );
	testNum = RSADecrypt( testNum, d, n );
	printf( "message = %s\n", bc_num2str(testNum) );

	return 0;
}
