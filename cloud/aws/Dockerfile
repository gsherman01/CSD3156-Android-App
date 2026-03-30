FROM public.ecr.aws/lambda/python:3.12

# Install Python dependencies into the Lambda task root.
COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt --target "${LAMBDA_TASK_ROOT}"

# Copy only backend service code required at runtime.
COPY backend "${LAMBDA_TASK_ROOT}/backend"

# Lambda handler
CMD ["backend.lambda_handler.handler"]
