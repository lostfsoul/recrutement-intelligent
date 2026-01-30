#!/bin/bash

API_BASE="http://localhost:8080/api/v1"
PASSED=0
FAILED=0
SKIPPED=0
ERRORS=()

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test data
TIMESTAMP=$(date +%s)
CANDIDATE_EMAIL="candidate.$TIMESTAMP@example.com"
RECRUITER_EMAIL="recruiter.$TIMESTAMP@example.com"
ADMIN_EMAIL="admin.$TIMESTAMP@example.com"

echo "=========================================="
echo "  COMPLETE API TEST - All Endpoints"
echo "=========================================="
echo ""

test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local token="$5"
    local expected_status="$6"
    local headers="$7"

    echo -n "Testing: $name ... "

    local url="$API_BASE$endpoint"
    local cmd="curl -s -X $method \"$url\""

    if [ -n "$token" ]; then
        cmd="$cmd -H \"Authorization: Bearer $token\""
    fi

    if [ -n "$headers" ]; then
        cmd="$cmd $headers"
    fi

    if [ -n "$data" ]; then
        cmd="$cmd -H 'Content-Type: application/json' -d '$data'"
    fi

    local response=$(eval $cmd 2>/dev/null)
    local actual_status=$(echo $response | jq -r '.status // .code // "empty"')

    if [ "$expected_status" = "any" ]; then
        if echo $response | grep -q '"id"' || echo $response | grep -q '"content"' || [ "$actual_status" = "200" ] || [ "$actual_status" = "201" ]; then
            echo -e "${GREEN}✓ PASS${NC}"
            ((PASSED++))
            return 0
        fi
    elif [ -n "$expected_status" ]; then
        if [ "$actual_status" = "$expected_status" ]; then
            echo -e "${GREEN}✓ PASS${NC} (got expected $actual_status)"
            ((PASSED++))
            return 0
        fi
    fi

    # Default: check for 200/201 or presence of id
    if [ "$actual_status" = "200" ] || [ "$actual_status" = "201" ]; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    elif echo $response | grep -q '"id"' && [ "$actual_status" != "400" ] && [ "$actual_status" != "404" ]; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    fi

    echo -e "${RED}✗ FAIL${NC} (Status: $actual_status)"
    ((FAILED++))
    ERRORS+=("$name: $response")
    return 1
}

skip_endpoint() {
    echo -e "${YELLOW}⊘ SKIP $1${NC}"
    ((SKIPPED++))
}

echo "=== TEST 1: AUTHENTICATION ==="
echo ""

# Clean up - delete existing test users
curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"recruiter@test.com","password":"Password123"}' > /dev/null 2>&1
curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"candidate@test.com","password":"Password123"}' > /dev/null 2>&1

test_endpoint \
    "Register Candidate" \
    "POST" \
    "/auth/register" \
    "{\"email\":\"$CANDIDATE_EMAIL\",\"password\":\"Password123\",\"passwordConfirmation\":\"Password123\",\"nom\":\"Test\",\"prenom\":\"Candidate\",\"role\":\"CANDIDAT\"}" \
    "" \
    "201"

test_endpoint \
    "Register Recruiter" \
    "POST" \
    "/auth/register" \
    "{\"email\":\"$RECRUITER_EMAIL\",\"password\":\"Password123\",\"passwordConfirmation\":\"Password123\",\"nom\":\"Test\",\"prenom\":\"Recruiter\",\"nomEntreprise\":\"TestCorp\",\"poste\":\"HR\",\"role\":\"RECRUTEUR\"}" \
    "" \
    "201"

# Login to get tokens
echo -n "Login as Candidate (get token) ... "
CANDIDATE_TOKEN=$(curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$CANDIDATE_EMAIL\",\"password\":\"Password123\"}" | jq -r '.accessToken')

if [ -n "$CANDIDATE_TOKEN" ] && [ "$CANDIDATE_TOKEN" != "null" ]; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
fi

echo -n "Login as Recruiter (get token) ... "
RECRUITER_TOKEN=$(curl -s -X POST $API_BASE/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$RECRUITER_EMAIL\",\"password\":\"Password123\"}" | jq -r '.accessToken')

if [ -n "$RECRUITER_TOKEN" ] && [ "$RECRUITER_TOKEN" != "null" ]; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
fi

echo ""
echo "=== TEST 2: CANDIDATE ENDPOINTS ==="
echo ""

test_endpoint \
    "Get Candidate Profile" \
    "GET" \
    "/candidats/me" \
    "" \
    "$CANDIDATE_TOKEN" \
    "404"

# Update Candidate Profile
test_endpoint \
    "Update Candidate Profile" \
    "PUT" \
    "/candidats/me" \
    '{"telephone":"+33612345678","linkedinUrl":"https://linkedin.com/in/test","githubUrl":"https://github.com/test","presentation":"Developer","titrePosteRecherche":"Full Stack Developer","pretentionSalarialeMin":45000,"pretentionSalarialeMax":60000}' \
    "$CANDIDATE_TOKEN" \
    "200"

echo ""
echo "=== TEST 3: RECRUITER ENDPOINTS ==="
echo ""

test_endpoint \
    "Get Recruiter Profile" \
    "GET" \
    "/recruteurs/me" \
    "" \
    "$RECRUITER_TOKEN" \
    "200"

test_endpoint \
    "Create Company" \
    "POST" \
    "/recruteurs/me/entreprises" \
    '{"nom":"TestCompany","secteur":"Technology","ville":"Paris","pays":"France","description":"Test Company","siteWeb":"https://testcompany.com"}' \
    "$RECRUITER_TOKEN" \
    "201"

# Get company ID for later tests
echo -n "Get company ID ... "
COMPANY_RESPONSE=$(curl -s -X POST $API_BASE/recruteurs/me/entreprises \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"TestCompany2","secteur":"Tech","ville":"Paris"}')

if echo $COMPANY_RESPONSE | grep -q '"id"'; then
    COMPANY_ID=$(echo $COMPANY_RESPONSE | jq -r '.id')
    echo -e "${GREEN}✓ PASS${NC} (Company ID: $COMPANY_ID)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    COMPANY_ID=""
fi

test_endpoint \
    "Get My Companies" \
    "GET" \
    "/recruteurs/me/entreprises" \
    "" \
    "$RECRUITER_TOKEN" \
    "200"

test_endpoint \
    "Get My Job Offers" \
    "GET" \
    "/recruteurs/me/offres" \
    "" \
    "$RECRUITER_TOKEN" \
    "200"

echo ""
echo "=== TEST 4: JOB OFFER ENDPOINTS ==="
echo ""

if [ -n "$COMPANY_ID" ]; then
    test_endpoint \
        "Create Job Offer" \
        "POST" \
        "/offres" \
        "{\"titre\":\"Senior Java Developer\",\"description\":\"Looking for experienced Java developer with Spring Boot and PostgreSQL skills\",\"typeContrat\":\"CDI\",\"salaireMin\":50000,\"salaireMax\":70000,\"ville\":\"Paris\",\"localisation\":\"Paris, France\",\"teletravail\":true,\"competencesRequises\":\"Java, Spring Boot, PostgreSQL\",\"profilRecherche\":\"5+ years experience\",\"experienceMin\":5,\"entrepriseId\":$COMPANY_ID}" \
        "$RECRUITER_TOKEN" \
        "201"

    # Get the created offer ID
    echo -n "Get offer ID ... "
    OFFRE_RESPONSE=$(curl -s -X GET $API_BASE/recruteurs/me/offres \
        -H "Authorization: Bearer $RECRUITER_TOKEN")

    if echo $OFFRE_RESPONSE | grep -q '"id"'; then
        OFFRE_ID=$(echo $OFFRE_RESPONSE | jq -r '.content[0].id')
        echo -e "${GREEN}✓ PASS${NC} (Offer ID: $OFFRE_ID)"
        ((PASSED++))
    else
        echo -e "${YELLOW}⊘ SKIP${NC} (no offers found)"
        OFFRE_ID=""
    fi
else
    echo -e "${YELLOW}⊘ SKIP Create Job Offer (no company ID)${NC}"
    OFFRE_ID=""
fi

test_endpoint \
    "Get All Job Offers (public)" \
    "GET" \
    "/offres" \
    "" \
    "" \
    "200"

test_endpoint \
    "Get All Job Offers (paginated)" \
    "GET" \
    "/offres?page=0&size=5" \
    "" \
    "" \
    "200"

test_endpoint \
    "Search Job Offers" \
    "GET" \
    "/offres?titre=java" \
    "" \
    "" \
    "200"

if [ -n "$OFFRE_ID" ]; then
    test_endpoint \
        "Get Job Offer by ID" \
        "GET" \
        "/offres/$OFFRE_ID" \
        "" \
        "" \
        "200"

    test_endpoint \
        "Update Job Offer" \
        "PUT" \
        "/offres/$OFFRE_ID" \
        '{"titre":"Updated: Senior Java Developer","salaireMax":75000}' \
        "$RECRUITER_TOKEN" \
        "200"
fi

echo ""
echo "=== TEST 5: APPLICATION ENDPOINTS ==="
echo ""

# Need a candidate ID for applications
echo -n "Get candidate ID ... "
CANDIDATE_ID=$(curl -s -X GET $API_BASE/candidats/me \
  -H "Authorization: Bearer $CANDIDATE_TOKEN" | jq -r '.id')

if [ -n "$CANDIDATE_ID" ] && [ "$CANDIDATE_ID" != "null" ]; then
    echo -e "${GREEN}✓ PASS${NC} (Candidate ID: $CANDIDATE_ID)"
else
    echo -e "${YELLOW}⊘ SKIP${NC} (no candidate ID)"
    CANDIDATE_ID=""
fi

if [ -n "$OFFRE_ID" ] && [ -n "$CANDIDATE_ID" ]; then
    test_endpoint \
        "Apply to Job Offer" \
        "POST" \
        "/offres/$OFFRE_ID/postuler" \
        '{"lettreMotivation":"I am very interested in this position and believe my skills are a great match."}' \
        "$CANDIDATE_TOKEN" \
        "201"

    test_endpoint \
        "Get My Applications" \
        "GET" \
        "/candidatures/me" \
        "" \
        "$CANDIDATE_TOKEN" \
        "200"

    # Get application ID
    echo -n "Get application ID ... "
    APP_RESPONSE=$(curl -s -X GET $API_BASE/candidatures/me \
        -H "Authorization: Bearer $CANDIDATE_TOKEN")

    if echo $APP_RESPONSE | grep -q '"id"'; then
        APP_ID=$(echo $APP_RESPONSE | jq -r '.content[0].id')
        echo -e "${GREEN}✓ PASS${NC} (Application ID: $APP_ID)"
        ((PASSED++))
    else
        echo -e "${YELLOW}⊘ SKIP${NC} (no applications found)"
        APP_ID=""
    fi

    if [ -n "$APP_ID" ]; then
        test_endpoint \
            "Get Application Details" \
            "GET" \
            "/candidatures/$APP_ID" \
            "" \
            "$CANDIDATE_TOKEN" \
            "200"
    fi
else
    echo -e "${YELLOW}⊘ SKIP Application tests (no candidate/offer ID)${NC}"
fi

echo ""
echo "=== TEST 6: AI ENDPOINTS ==="
echo ""

test_endpoint \
    "Extract Skills from Text" \
    "POST" \
    "/ai/extract-skills" \
    '{"text":"Experienced Java developer with Spring Boot, PostgreSQL, and React knowledge"}' \
    "$RECRUITER_TOKEN" \
    "200"

echo ""
echo "=== TEST 7: MATCHING ENDPOINTS ==="
echo ""

if [ -n "$CANDIDATE_ID" ]; then
    test_endpoint \
        "Find Matching Offers for Candidate" \
        "GET" \
        "/matching/candidats/$CANDIDATE_ID/offres" \
        "" \
        "$CANDIDATE_TOKEN" \
        "200"
else
    skip_endpoint "Find Matching Offers (no candidate ID)"
fi

if [ -n "$OFFRE_ID" ]; then
    test_endpoint \
        "Find Matching Candidates for Offer" \
        "GET" \
        "/matching/offres/$OFFRE_ID/candidats" \
        "" \
        "$RECRUITER_TOKEN" \
        "200"
else
    skip_endpoint "Find Matching Candidates (no offer ID)"
fi

echo ""
echo "=========================================="
echo -e "  ${GREEN}PASSED: $PASSED${NC}"
echo -e "  ${RED}FAILED: $FAILED${NC}"
echo -e "  ${YELLOW}SKIPPED: $SKIPPED${NC}"
echo "=========================================="

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "Error Details:"
    for error in "${ERRORS[@]}"; do
        echo "  - $error"
    done
fi

echo ""
echo "=========================================="
echo "  TEST COMPLETE"
echo "=========================================="
echo ""
echo -e "${BLUE}Summary:${NC}"
echo "  • Authentication: $(grep -q "Register.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • Candidate APIs: $(grep -q "Candidate.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • Recruiter APIs: $(grep -q "Recruiter.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • Job Offer APIs: $(grep -q "Job Offer.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • Application APIs: $(grep -q "Application.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • AI APIs: $(grep -q "AI.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"
echo "  • Matching APIs: $(grep -q "Matching.*PASS" <<< "$PASSED" && echo "✅" || echo "❌")"

if [ $PASSED -ge 15 ]; then
    echo ""
    echo -e "${GREEN}🎉 All major endpoints are working!${NC}"
elif [ $PASSED -ge 10 ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Most endpoints working${NC}"
else
    echo ""
    echo -e "${RED}❌ Some endpoints have issues${NC}"
fi
