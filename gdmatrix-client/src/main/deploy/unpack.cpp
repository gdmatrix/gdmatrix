#ifndef UNICODE
#define UNICODE
#endif 

#include <Windows.h>
#include <stdio.h>
#include <process.h>
#include <direct.h>
#include <time.h>

#define BUFFER_SIZE 256

int buffer[BUFFER_SIZE];
int bufferSize = 0;

#define MARK_LENGTH 8
int MARK[] = { '=', '=', '@', 'F', 'I', 'L', 'E', ':' };


int readBuffer(FILE* file)
{
	int ch;
	if (bufferSize == 0)
	{
		ch = fgetc(file);
	}
	else
	{
		ch = buffer[bufferSize - 1];
		bufferSize--;
	}
	return ch;
}

void unreadBuffer(int ch)
{
	buffer[bufferSize++] = ch;
}

bool startsMark(FILE* file)
{
	int ch;
	int i = 0;
	while (i < MARK_LENGTH)
	{
		ch = readBuffer(file);
		if (ch == MARK[i])
		{
			i++;
		}
		else break;
	}
	if (i == MARK_LENGTH)
	{
		bufferSize = 0;
		return true;
	}
	else
	{
		unreadBuffer(ch);
		for (int j = i - 1; j >= 0; j--)
		{
			unreadBuffer(MARK[j]);
		}
		return false;
	}
}

void readFileName(FILE* file, char* fileName, int size)
{
	int pos = 0;
	int ch = fgetc(file);
	while (ch != EOF && ch != 10 && pos < size)
	{
		if (ch >= 32)
		{
			fileName[pos++] = (char)ch;
		}
		ch = fgetc(file);
	}
	fileName[pos] = 0;
}

void getExeName(char* filePath, char* exeName)
{
	size_t len = strlen(filePath);
	char* p = filePath + len;
	// position p at last ocurrence of slash
	while (p != filePath) 
	{
		if (*p == '\\')
		{
			p++;
			break;
		}
		else
		{
			p--;
		}
	}
	// get only letters from file name
	int i = 0;
	while (*p != 0)
	{
		char ch = *p;
		if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
		{
			exeName[i++] = ch;
		}
		p++;
	}
	exeName[i] = 0;
}

int main(void)
{
	printf("Unpack v1.0\n");

	errno_t err;
	char scriptPath[256] = "";

	char filePath[256];
	GetModuleFileNameA(NULL, filePath, 256);
	printf("File path: %s\n", filePath);

	char extractDir[256];
	GetTempPathA(256, extractDir);

	char exeName[256];
	getExeName(filePath, exeName);

	strcat_s(extractDir, exeName);
	printf("Extraction directory: %s\n", extractDir);
	int mkErr = _mkdir(extractDir);

	FILE* file = NULL;
	err = fopen_s(&file, filePath, "rb");
	if (err != 0 || file == NULL)
	{
		printf("Can't read executable.");
		return 1;
	}

	FILE* part = NULL;
	bool end = false;
	int partCount = 0;
	do
	{
		if (startsMark(file))
		{
			if (part)
			{
				// close previous part
				fclose(part);
			}
			char partFileName[256] = "";
			readFileName(file, partFileName, 256);

			char partPath[1024] = "";
			sprintf_s(partPath, "%s\\%s", extractDir, partFileName);
			printf("Part path: %s\n", partPath);

			partCount++;
			if (partCount == 1) // if first part
			{
				strcpy_s(scriptPath, partPath);
			}
			err = fopen_s(&part, partPath, "wb");
		}
		else
		{
			int c = readBuffer(file);
			if (c == EOF)
			{
				end = true;
				if (part)
				{
					fclose(part);
				}
			}
			else if (part)
			{
				fputc(c, part);
			}
		}
	} while (err == 0 && !end);

	fclose(file);
	if (err == 0)
	{
		printf("%i parts extracted.\n", partCount);
		if (partCount > 0)
		{
			printf("Running scipt %s\n", scriptPath);
			_execlp("cmd", "/c", scriptPath, NULL);
		}
		return 0;
	}
	else
	{
		printf("Cant open file part.");
		return 1;
	}
}