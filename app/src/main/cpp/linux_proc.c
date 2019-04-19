//
// Created by czf on 19-4-18.
//

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>

int main(int argc, char *argv[], char *env[]) {
    const char *filePath = "/sdcard/linux_proc.log";
    int fd = open(filePath, O_CREAT | O_RDWR);
    if (fd == 0) {
        return -1;
    }
    while (1) {
        write(fd, "hello world\n", 12);
        sleep(1);
    }
}
