#!/usr/bin/env bash
aws s3 cp tests/testlambda/target/function.zip s3://teeeeeweeeeest/package.zip
aws cloudformation deploy --stack-name customlambda --template-file cf-function.yml --capabilities CAPABILITY_NAMED_IAM --parameter-overrides StackIdentifier=lambdaruntime
