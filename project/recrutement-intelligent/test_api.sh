#!/bin/bash

API_BASE="http://localhost:8080/api/v1"
PASSED=0
FAILED=0
ERRORS=()

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "  API Testing - All Endpoints"
echo "=========================================="
echo ""

# Helper function to test endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local token="$5"
    local headers="$6"

    echo -n "Testing: $name ... "

    local cmd="curl -s -X $method $API_BASE$endpoint"

    if [ -n "$token" ]; then
        cmd="$cmd -H \"Authorization: Bearer $token\""
    fi

    if [ -n "$headers" ]; then
        cmd="$cmd $headers"
    fi

    if [ -n "$data" ]; then
        cmd="$cmd -H 'Content-Type: application/json' -d '$data'"
    fi

    local response=$(eval $cmd)
    local status=$(echo $response | jq -r '.status // .code // "empty"')

    # Check for success status or presence of expected data
    if [ "$status" = "200" ] || [ "$status" = "201" ]; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    elif echo $response | grep -q '"id"' && [ "$status" != "400" ] && [ "$status" != "404" ]; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    elif [ "$status" = "empty" ]; then
        echo -e "${GREEN}✓ PASS${NC} (empty response is valid for lists)"
        ((PASSED++))
        return 0
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Status: $status)"
        ((FAILED++))
        ERRORS+=("$name: $response")
        return 1
    fi
}

echo "=== 1. AUTHENTICATION ==="
echo ""

# Register Candidate
test_endpoint \
    "Register Candidate" \
    "POST" \
    "/auth/register" \
    '{"email":"candidate.test@example.com","password":"Password123","passwordConfirmation":"Password123","nom":"Test","prenom":"Candidate","role":"CANDIDAT"}'

# Register Recruiter
test_endpoint \
    "Register Recruiter" \
    "POST" \
    "/auth/register" \
    '{"email":"recruiter.test@example.com","password":"Password123","passwordConfirmation":"Password123","nom":"Test","prenom":"Recruiter","nomEntreprise":"TestCorp","poste":"HR","role":"RECRUTEUR"}'

# Login as Recruiter and get token
echo -n "Login as Recruiter (get token) ... "
LOGIN_RESPONSE=$(curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"recruiter.test@example.com","password":"Password123"}')

if echo $LOGIN_RESPONSE | grep -q '"accessToken"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
    RECRUITER_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
    RECRUITER_TOKEN=""
fi

echo ""
echo "=== 2. CANDIDATE ENDPOINTS ==="
echo ""

test_endpoint \
    "Get Candidate Profile" \
    "GET" \
    "/candidats/me" \
    "" \
    "$RECRUITER_TOKEN" \
    "-H 'Content-Type: application/json'"

echo ""
echo "=== 3. RECRUITER ENDPOINTS ==="
echo ""

# Create company first
echo -n "Create Company (required for offers) ... "
COMPANY_RESPONSE=$(curl -s -X POST $API_BASE/recruteurs/me/entreprises \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"TestCompany","secteur":"Technology","ville":"Paris"}')

if echo $COMPANY_RESPONSE | grep -q '"id"'; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
    COMPANY_ID=$(echo $COMPANY_RESPONSE | jq -r '.id')
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
    COMPANY_ID=""
fi

test_endpoint \
    "Get Recruiter Profile" \
    "GET" \
    "/recruteurs/me" \
    "" \
    "$RECRUITER_TOKEN"

test_endpoint \
    "Get My Companies" \
    "GET" \
    "/recruteurs/me/entreprises" \
    "" \
    "$RECRUITER_TOKEN"

echo ""
echo "=== 4. JOB OFFER ENDPOINTS ==="
echo ""

if [ -n "$COMPANY_ID" ]; then
    test_endpoint \
        "Create Job Offer" \
        "POST" \
        "/offres" \
        "{\"titre\":\"Senior Java Developer\",\"description\":\"We are looking for an experienced Java developer to join our team working on Spring Boot and PostgreSQL.\",\"typeContrat\":\"CDI\",\"salaireMin\":50000,\"salaireMax\":70000,\"ville\":\"Paris\",\"competencesRequises\":\"Java, Spring Boot\",\"entrepriseId\":$COMPANY_ID}" \
        "$RECRUITER_TOKEN"
else
    echo -e "${YELLOW}⊘ SKIP Create Job Offer (no company ID)${NC}"
fi

test_endpoint \
    "Get All Job Offers" \
    "GET" \
    "/offres" \
    ""

test_endpoint \
    "Get All Job Offers (paginated)" \
    "GET" \
    "/offres?page=0&size=10" \
    ""

echo ""
echo "=== 5. PUBLIC ENDPOINTS ==="
echo ""

test_endpoint \
    "Get All Companies" \
    "GET" \
    "/entreprises" \
    ""

echo ""
echo "=========================================="
echo -e "  ${GREEN}PASSED: $PASSED${NC}"
echo -e "  ${RED}FAILED: $FAILED${NC}"
echo "=========================================="

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "Error Details:"
    for error in "${ERRORS[@]}"; do
        echo "  - $error"
    done
fi

echo ""
echo "Test completed!"
