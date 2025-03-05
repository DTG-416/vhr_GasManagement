#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
using namespace std;

vector<string> s; // 用于存储读取的字符串
int cnt = 0; // 计数器
string x;

void solve1()
{
    ifstream infile("data.csv"); // 打开文件 "data.csv" 进行读取
    ofstream outfile("over1.csv"); // 打开文件 "over1.csv" 进行写入

    char c;
    while (infile.get(c))
    {
        if (c == '\t')
            outfile << ','; // 将制表符替换为逗号
        else
            outfile.put(c); // 否则直接写入字符
    }

    infile.close();
    outfile.close();
}

void solve2()
{
    ifstream infile("over1.csv"); // 打开文件 "over1.csv" 进行读取
    ofstream outfile("over2.csv"); // 打开文件 "over2.csv" 进行写入

    while (infile >> x) // 逐个单词读取
    {
        if (x[0] == '#') // 遇到以 '#' 开头的字符串
        {
            for (int i = 0; i < cnt; i++)
            {
                if (i > 0)
                    outfile << ','; // 如果不是第一个元素，插入逗号
                outfile << s[i];
            }
            outfile << '\n';
            cnt = 0; // 重置计数器
        }
        s.push_back(x); // 存储单词
        cnt++;
    }

    // 输出剩余的内容
    for (int i = 0; i < cnt; i++)
    {
        if (i > 0)
            outfile << ',';
        outfile << s[i];
    }

    infile.close();
    outfile.close();
}

void solve3()
{
    ifstream infile("over2.csv"); // 打开文件 "over2.csv" 进行读取
    ofstream outfile("over3.csv"); // 打开文件 "over3.csv" 进行写入

    char c, last = 0;
    while (infile.get(c))
    {
        if (c == ',' && last == ',') // 如果遇到连续的逗号
        {
            outfile.put(','); // 输出一个逗号
        }
        outfile.put(c); // 输出当前字符
        last = c; // 更新 last
    }

    infile.close();
    outfile.close();
}

int main()
{
    solve1(); // 处理文件1
    solve2(); // 处理文件2
    solve3(); // 处理文件3
    return 0;
}
