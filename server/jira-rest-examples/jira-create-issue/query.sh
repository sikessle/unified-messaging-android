#!/bin/bash

curl -D- -u sikessle:sikessle -X POST -H "Content-Type: application/json" 	\
http://metaproject.in.fhkn.de:8080/rest/api/2/issue					        \
--data-binary @params.json \
> response.json

echo ""
echo ""
echo ""