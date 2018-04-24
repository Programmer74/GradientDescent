#!/bin/bash
if [ -z "$2" ]; then
	echo "Usage: $0 <dataset size> <output file name>"
	exit
fi
perl -E "
	for(\$i=0;\$i<$1;\$i++){
		\$x = rand 10000;
		\$x = \$x / 10000;
		\$y = \$x + 2;
		say \"\$x,\$y\";
	}" > $2
