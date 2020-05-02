#!/bin/bash
for id in 2058017 2058016 2058015 2058014 2058012 2058013 2058011 2058010 2058009 2058008 2058007 2058006 2058005 2058004 2058002 2058003 2057994 2057995 2058000 2058001 2057982 2057983 2057989 2057988 2057976 2057977 2057971 2057970 2057965 2057964 2057959 2057958 2057998 2057999 2057993 2057986 2057987 2057992 2057981 2057975 2057980 2057974 2057968 2057969 2057963 2057957 2057962 2057956 2057996 2057997 2057991 2057990 2057985 2057978 2057984 2057979 2057973 2057967 2057972 2057966 2057960 2057961 2057955 2057954
do
  printf "===========================================================\n"
  printf "Fetching match $id from dataset. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id=$id --type=edn
  clj src/main/io/spit_match.clj --id=$id --type=json
  printf "===========================================================\n"
  printf "Creating graph...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id=$id --type=edn
  clj src/main/io/spit_graph.clj --id=$id --type=json
  printf "===========================================================\n"
  printf "Calculating metrics...\n"
  printf "This operations may take a while, please be patient...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json
  printf "Done!\n"
done
