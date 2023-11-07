Here we give two sh files. These files need to be used on Google Cloud and bigQuery is enabled. For relevant knowledge,
you can view the [Google Dataset Documentation](https://github.com/google/cluster-data/blob/master/ClusterData2019.md).

* `generateGoogleTraceHostResource.sh`: Query the number of hosts for each cpu and memory combination in a cluster in
  the Google trace dataset and save it in a csv file. Since the CPU and memory are normalized, and the resources of
  CPNSim are represented by the int type, they are numerically expanded when converted into the host simulated by
  CPNSim.
* `generateGoogleTraceUserRequest.sh`: Query the submission of user tasks at different times in a cluster in the Google
  trace dataset, as well as the resource request status of each user task. A user set here is a userRequest of CPNSim, a
  collection is an instanceGroup of CPNSim, and an instance is an instance of CPNSim.