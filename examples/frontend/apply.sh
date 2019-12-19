#!/bin/bash
kubectl apply -f <(istioctl kube-inject -f frontend.yaml) -n b3inject
