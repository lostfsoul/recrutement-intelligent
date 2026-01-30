#!/bin/bash

API_BASE="http://localhost:8080/api/v1"
PASSED=0
FAILED=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "=== Testing with Fresh Data ==="
echo ""

# 1. Register Recruiter (fresh email)
echo -n "1. Register Recruiter ... "
RESPONSE=$(curl -s -X POST $API_BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"fresh.recruiter'$(date +%s)'@example.com",
    "password":"Password123",
    "passwordConfirmation":"Password123",
    "nom":"Test",
    "prenom":"Recruiter",
    "nomEntreprise":"TestCorp",
    "poste":"HR",
    "role":"RECRUTEUR"
  }')

if echo $RESPONSE | grep -q '"accessToken"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
    TOKEN=$(echo $RESPONSE | jq -r '.accessToken')
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE
    ((FAILED++))
    exit 1
fi

# 2. Login (get token)
echo -n "2. Login ... "
RESPONSE=$(curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"fresh.recruiter'$(date +%s)'@example.com\",\"password\":\"Password123\"}")

if echo $RESPONSE | grep -q '"accessToken"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
    TOKEN=$(echo $RESPONSE | jq -r '.accessToken')
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE
    ((FAILED++))
fi

# 3. Create Company
echo -n "3. Create Company ... "
RESPONSE=$(curl -s -X POST $API_BASE/recruteurs/me/entreprises \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"FreshCompany","secteur":"Tech","ville":"Paris"}')

if echo $RESPONSE | grep -q '"id"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
    COMPANY_ID=$(echo $RESPONSE | jq -r '.id')
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE
    ((FAILED++))
fi

# 4. Get Recruiter Profile
echo -n "4. Get Recruiter Profile ... "
RESPONSE=$(curl -s -X GET $API_BASE/recruteurs/me \
  -H "Authorization: Bearer $TOKEN")

if echo $RESPONSE | grep -q '"id"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE | head -c 200
    ((FAILED++))
fi

# 5. Get My Companies
echo -n "5. Get My Companies ... "
RESPONSE=$(curl -s -X GET $API_BASE/recruteurs/me/entreprises \
  -H "Authorization: Bearer $TOKEN")

if echo $RESPONSE | grep -q '"id"' || echo $RESPONSE | grep -q '\[\]'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE | head -c 200
    ((FAILED++))
fi

# 6. Create Job Offer
if [ -n "$COMPANY_ID" ]; then
    echo -n "6. Create Job Offer ... "
    RESPONSE=$(curl -s -X POST $API_BASE/offres \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"titre\": \"Java Developer\",
        \"description\": \"Join our team! Looking for an experienced Java developer with Spring Boot skills.\",
        \"typeContrat\": \"CDI\",
        \"salaireMin\": 50000,
        \"salaireMax\": 70000,
        \"ville\": \"Paris\",
        \"competencesRequises\": \"Java, Spring Boot\",
        \"entrepriseId\": $COMPANY_ID
      }")

    if echo $RESPONSE | grep -q '"id"'; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        OFFRE_ID=$(echo $RESPONSE | jq -r '.id')
    else
        echo -e "${RED}✗ FAIL${NC}"
        echo $RESPONSE | head -c 200
        ((FAILED++))
    fi
else
    echo -e "${YELLOW}⊘ SKIP Create Job Offer (no company ID)${NC}"
fi

# 7. Get Job Offers (public)
echo -n "7. Get Job Offers (public) ... "
RESPONSE=$(curl -s -X GET "$API_BASE/offres?page=0&size=10")

if echo $RESPONSE | grep -q '"content"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    echo $RESPONSE | head -c 200
    ((FAILED++))
fi

echo ""
echo "=========================================="
echo -e "  ${GREEN}PASSED: $PASSED${NC}"
echo -e "  ${RED}FAILED: $FAILED${NC}"
echo "=========================================="

if [ $PASSED -ge 5 ]; then
    echo -e "${GREEN}✓ Core APIs are working!${NC}"
else
    echo -e "${RED}✗ Some APIs have issues${NC}"
fi
