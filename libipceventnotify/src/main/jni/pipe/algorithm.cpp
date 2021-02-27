#include "algorithm.h"


void kmp_next(const char* key,int *next,int key_len){
    int i=1;
    next[1]=0;
    int j=0;
    while (i<key_len) {
        if (j==0||key[i-1]==key[j-1]) {
            i++;
            j++;
            next[i]=j;
        }else{
            j=next[j];
        }
    }
}

int kmp_search(char * arr,const char * key,int next[],int arr_len,int key_len){
    int i=1;
    int j=1;
    while (i<=arr_len&&j<=key_len) {
        if (j==0 || arr[i-1]==key[j-1]) {
            i++;
            j++;
        }
        else{
            j=next[j];
        }
    }
    if (j>key_len) {
        return i-(int)key_len - 1;
    }
    return -1;
}


int bf_searchkey(char* arr, int begin_position,const char* key,int arr_len,int key_len)
{
	if (arr == NULL || arr_len <= begin_position)
		return -1;

	if (key == NULL || arr_len < key_len)
		return -1;

	int i = -1;
	int j = -1;
	for (i = begin_position; i < arr_len; i++)
	{
		if (arr_len < key_len + i)
			break;

		for (j = 0; j < key_len; j++)
		{
			if (arr[i+j] != key[j])
				break;
		}
		if (j == key_len)
			return i;
	}
	return -1;
}
