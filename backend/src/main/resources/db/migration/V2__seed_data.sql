-- ─── Seed Topics ──────────────────────────────────────────────
INSERT INTO topics (id, name, description, external_api_ref)
VALUES
    (gen_random_uuid(), 'Technology', 'Words related to technology, computers, and software', 'technology'),
    (gen_random_uuid(), 'Business', 'Essential business and finance vocabulary', 'business'),
    (gen_random_uuid(), 'Nature', 'Words describing nature, environment, and ecology', 'nature'),
    (gen_random_uuid(), 'Health & Medicine', 'Medical and health-related terminology', 'health'),
    (gen_random_uuid(), 'Daily Conversation', 'Common words used in everyday English conversation', 'conversation');

-- ─── Seed Vocabularies (Technology Topic) ─────────────────────
WITH tech_topic AS (SELECT id FROM topics WHERE name = 'Technology')
INSERT INTO vocabularies (id, topic_id, word, definition, pronunciation, part_of_speech, example_sentence, difficulty)
VALUES
    (gen_random_uuid(), (SELECT id FROM tech_topic), 'algorithm',
     'A process or set of rules to be followed in calculations or problem-solving operations',
     '/ˈælɡərɪðəm/', 'noun',
     'The search algorithm returns results sorted by relevance.',
     'MEDIUM'),
    (gen_random_uuid(), (SELECT id FROM tech_topic), 'bandwidth',
     'The maximum rate of data transfer across a given path',
     '/ˈbændwɪdθ/', 'noun',
     'We need more bandwidth to stream high-definition videos.',
     'EASY'),
    (gen_random_uuid(), (SELECT id FROM tech_topic), 'encryption',
     'The process of converting information into a code to prevent unauthorized access',
     '/ɪnˈkrɪpʃn/', 'noun',
     'End-to-end encryption protects your messages from being read by others.',
     'MEDIUM'),
    (gen_random_uuid(), (SELECT id FROM tech_topic), 'latency',
     'The delay before a transfer of data begins following an instruction',
     '/ˈleɪtənsi/', 'noun',
     'High latency causes noticeable delays in online gaming.',
     'HARD'),
    (gen_random_uuid(), (SELECT id FROM tech_topic), 'scalability',
     'The capacity to be changed in size or scale; ability to handle growth',
     '/ˌskeɪləˈbɪlɪti/', 'noun',
     'The new architecture improves the scalability of our platform.',
     'HARD');

-- ─── Seed Vocabularies (Business Topic) ───────────────────────
WITH biz_topic AS (SELECT id FROM topics WHERE name = 'Business')
INSERT INTO vocabularies (id, topic_id, word, definition, pronunciation, part_of_speech, example_sentence, difficulty)
VALUES
    (gen_random_uuid(), (SELECT id FROM biz_topic), 'revenue',
     'Income generated from normal business operations',
     '/ˈrevənjuː/', 'noun',
     'The company reported a 20% increase in annual revenue.',
     'EASY'),
    (gen_random_uuid(), (SELECT id FROM biz_topic), 'leverage',
     'Use of borrowed capital to increase the potential return of an investment',
     '/ˈliːvərɪdʒ/', 'noun',
     'They used financial leverage to expand the business rapidly.',
     'MEDIUM'),
    (gen_random_uuid(), (SELECT id FROM biz_topic), 'acquisition',
     'The purchase of one business or company by another',
     '/ˌækwɪˈzɪʃn/', 'noun',
     'The acquisition of the startup was completed for $50 million.',
     'MEDIUM'),
    (gen_random_uuid(), (SELECT id FROM biz_topic), 'synergy',
     'The interaction of elements that when combined produce an effect greater than their individual parts',
     '/ˈsɪnədʒi/', 'noun',
     'The merger created strong synergy between the two departments.',
     'HARD'),
    (gen_random_uuid(), (SELECT id FROM biz_topic), 'stakeholder',
     'A person with an interest or concern in something, especially a business',
     '/ˈsteɪkhəʊldə/', 'noun',
     'All stakeholders were informed about the policy change.',
     'EASY');
