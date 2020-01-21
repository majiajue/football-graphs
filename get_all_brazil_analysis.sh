#!/bin/bash
for id in 2057978 2057980 2057982
do
  echo "Getting match $id"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  echo "Done with match $id"
done