#!/bin/bash

DATE=`date +%Y%m%d%H%M`
zip jemma-site-archive-$DATE.zip *.html images/* stylesheets/*
