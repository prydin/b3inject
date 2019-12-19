#!/bin/bash
kubectl apply -f <(istioctl kube-inject -f quoter.yaml) -n b3inject
