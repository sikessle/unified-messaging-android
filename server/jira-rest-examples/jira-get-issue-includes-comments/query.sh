#!/bin/bash

curl -D- -u sikessle:sikessle -X GET -H "Content-Type: application/json" 	\
http://metaproject.in.fhkn.de:8080/rest/api/2/issue/AUMEWT-106  \
> response.json

echo ""
echo ""
echo ""