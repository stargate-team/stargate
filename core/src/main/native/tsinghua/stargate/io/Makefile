DRIVER_DIR = /usr/local
LIB_NAME = pci
LINUX_EXTENSION = .so
FULL_LIB_NAME = lib$(LIB_NAME)$(LINUX_EXTENSION)
TARGET_DIR = ../../../../../../../lib_managed/native
TARGET_LIB = $(TARGET_DIR)/$(FULL_LIB_NAME)

CFLAGS 		:= -c -fPIC

INCLUDES	:= -I$(JAVA_HOME)/include \
		   -I$(JAVA_HOME)/include/linux \
		   -I$(DRIVER_DIR)/include

LIBRARIES   := -L$(DRIVER_DIR)/lib

ifeq ($(RELEASE),)
CFLAGS   	:= $(CFLAGS) -g
else
CFLAGS   	:= $(CFLAGS) -O2 -DNDEBUG
endif

COMPILE = $(CFLAGS) $(INCLUDES)

DEPS		:= IOUtils.o \
               FpgaDispatcherImpl.o \
               NativeThread.o

all: $(TARGET_LIB)

release:
	$(MAKE) RELEASE=1

$(TARGET_LIB): $(DEPS)
	mkdir -p $(TARGET_DIR)
	$(CC) -shared -o $@ $(DEPS) $(LIBRARIES) -lriffa

%.o: %.c
	$(CC) $(COMPILE) $< -o $@

.PYONY: all clean

clean:
	rm -f *.o
