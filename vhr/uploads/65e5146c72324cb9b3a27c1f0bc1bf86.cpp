#include <cstdio>
#include <iostream>
#include <cmath>
#include <algorithm>
#include <cstring>
#include <string>
#include <cstdlib>
#include <iostream>
using namespace std;
char s[100005];
int len;

void get()
{
	int i = 0;

	// 跳过空格和制表符
	while ((s[i] = getchar()) == ' ' || s[i] == '\t');

	// 如果是换行符，则停止
	if (s[i] == '\n') return;

	// 读取剩余的字符
	while ((s[++i] = getchar()) != '\n' && s[i] != EOF);

	// 保证字符串以'\0'结尾
	s[i] = '\0';

	len = i;
}

void res()
{
	memset(s,0,sizeof(char)*len);
	len=0;
}

void print()
{
	int p=0;
	for(;s[p]!='>';p++);
	for(++p;s[p]!='<';p++)cout<<s[p];
	cout<<',';
}

void rank1()
{
	get();
	print();
	res();
}

void user()
{
	for(int i = 1;i <= 5;i++)
	{
		get();
		res();
	}
	get();
	print();
	res();
	for(int i = 1;i <= 3;i++)
	{
		get();
		res();
	}
	get();
	if(s[1]=='!')
	{
		res();
		get();res();
	}
	else
	{
		res();
		while(get(),s[1]!='!')res();
		res();
	}
	get();res();
	get();res();
}

void all()
{
	get();res();
	get();
	print();
	res();
	get();res();
	get();
	print();
	res();
	get();res();
}

void single()
{
	get();res();
	get();
	if(s[1]=='!')
	{
		res();
		get();res();
		cout<<",,";
		return;
	}
	if(s[len-2]=='"')
	{
		res();
		for(int i = 1;i <= 5;i ++)get();res();
		cout<<"√,";
	}
	else
	{
		print();
		res();
	}
	get();res();
	get();
	if(s[1]=='!')
	{
		res();
		get();res();
		cout<<',';
	}
	else
	{
		print();
		res();
		get();res();
	}
}

void work()
{
	rank1();
	user();
	all();
	for(int i = 1;i <= 6;i ++)single();
	cout<<endl;
	get();res();
}

int main(){
	freopen("data.html","r",stdin);
	freopen("over.csv","w",stdout);
	get();
	res();
	while(get(),s[1]!='/')res(),work();
	
	return 0;
}
