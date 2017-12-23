#!/usr/bin/env bash

mvn clean compile dependency:copy-dependencies
rsync -azP rsync -azP ./ keesh.vm.mac:/home/keesh/Desktop/mtf-cms-server
