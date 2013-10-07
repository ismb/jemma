#!/bin/bash

DATE=`date +%Y%m%d%H%M`
zip site-archive-$DATE.zip *.html images/* stylesheets/*
