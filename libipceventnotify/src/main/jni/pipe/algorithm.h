#ifndef ALGORITHM_H_
#define ALGORITHM_H_

#include <stdio.h>
#include <string.h>

void kmp_next(const char*key,int *next,int key_len);

int kmp_search(char * arr,const char * key,int next[],int arr_len,int key_len);

int bf_searchkey(char* arr, int begin_position,const char* key,int arr_len,int key_len);


#endif /* ALGORITHM_H_ */
